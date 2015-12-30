;(function( $, $$ ){ 'use strict';

  var isObject = function(o){
    return o != null && typeof o === 'object';
  };

  var isFunction = function(o){
    return o != null && typeof o === 'function';
  };

  var isNumber = function(o){
    return o != null && typeof o === 'number';
  };

  var throttle = function(func, wait, options) {
    var leading = true,
        trailing = true;

    if (options === false) {
      leading = false;
    } else if (isObject(options)) {
      leading = 'leading' in options ? options.leading : leading;
      trailing = 'trailing' in options ? options.trailing : trailing;
    }
    options = options || {};
    options.leading = leading;
    options.maxWait = wait;
    options.trailing = trailing;

    return debounce(func, wait, options);
  };

  var debounce = function(func, wait, options) { // ported lodash debounce function
    var args,
        maxTimeoutId,
        result,
        stamp,
        thisArg,
        timeoutId,
        trailingCall,
        lastCalled = 0,
        maxWait = false,
        trailing = true;

    if (!isFunction(func)) {
      return;
    }
    wait = Math.max(0, wait) || 0;
    if (options === true) {
      var leading = true;
      trailing = false;
    } else if (isObject(options)) {
      leading = options.leading;
      maxWait = 'maxWait' in options && (Math.max(wait, options.maxWait) || 0);
      trailing = 'trailing' in options ? options.trailing : trailing;
    }
    var delayed = function() {
      var remaining = wait - (Date.now() - stamp);
      if (remaining <= 0) {
        if (maxTimeoutId) {
          clearTimeout(maxTimeoutId);
        }
        var isCalled = trailingCall;
        maxTimeoutId = timeoutId = trailingCall = undefined;
        if (isCalled) {
          lastCalled = Date.now();
          result = func.apply(thisArg, args);
          if (!timeoutId && !maxTimeoutId) {
            args = thisArg = null;
          }
        }
      } else {
        timeoutId = setTimeout(delayed, remaining);
      }
    };

    var maxDelayed = function() {
      if (timeoutId) {
        clearTimeout(timeoutId);
      }
      maxTimeoutId = timeoutId = trailingCall = undefined;
      if (trailing || (maxWait !== wait)) {
        lastCalled = Date.now();
        result = func.apply(thisArg, args);
        if (!timeoutId && !maxTimeoutId) {
          args = thisArg = null;
        }
      }
    };

    return function() {
      args = arguments;
      stamp = Date.now();
      thisArg = this;
      trailingCall = trailing && (timeoutId || !leading);

      if (maxWait === false) {
        var leadingCall = leading && !timeoutId;
      } else {
        if (!maxTimeoutId && !leading) {
          lastCalled = stamp;
        }
        var remaining = maxWait - (stamp - lastCalled),
            isCalled = remaining <= 0;

        if (isCalled) {
          if (maxTimeoutId) {
            maxTimeoutId = clearTimeout(maxTimeoutId);
          }
          lastCalled = stamp;
          result = func.apply(thisArg, args);
        }
        else if (!maxTimeoutId) {
          maxTimeoutId = setTimeout(maxDelayed, remaining);
        }
      }
      if (isCalled && timeoutId) {
        timeoutId = clearTimeout(timeoutId);
      }
      else if (!timeoutId && wait !== maxWait) {
        timeoutId = setTimeout(delayed, wait);
      }
      if (leadingCall) {
        isCalled = true;
        result = func.apply(thisArg, args);
      }
      if (isCalled && !timeoutId && !maxTimeoutId) {
        args = thisArg = null;
      }
      return result;
    };
  };

  function register( $$, $ ){

    // use a single dummy dom ele as target for every qtip
    var $qtipContainer = $('<div></div>');
    var viewportDebounceRate = 250;

    function generateOpts( target, passedOpts ){
      var qtip = target.scratch().qtip;
      var opts = $.extend( {}, passedOpts );

      if( !opts.id ){
        opts.id = 'cy-qtip-target-' + ( Date.now() + Math.round( Math.random() * 10000) );
      }

      if( !qtip.$domEle ){
        qtip.$domEle = $qtipContainer;
      }

      // qtip should be positioned relative to cy dom container
      opts.position = opts.position || {};
      opts.position.container = opts.position.container || $( document.body );
      opts.position.viewport = opts.position.viewport || $( document.body );
      opts.position.target = [0, 0];
      opts.position.my = opts.position.my || 'top center';
      opts.position.at = opts.position.at || 'bottom center';

      // adjust
      var adjust = opts.position.adjust = opts.position.adjust || {};
      adjust.method = adjust.method || 'flip';
      adjust.mouse = false;

      if( adjust.cyAdjustToEleBB === undefined ){
        adjust.cyAdjustToEleBB = true;
      }

      // default show event
      opts.show = opts.show || {};

      if( !opts.show.event ){
        opts.show.event = 'tap';
      }

      // default hide event
      opts.hide = opts.hide || {};
      opts.hide.cyViewport = opts.hide.cyViewport === undefined ? true : opts.hide.cyViewport;

      if( !opts.hide.event ){
        opts.hide.event = 'unfocus';
      }

      // so multiple qtips can exist at once (only works on recent qtip2 versions)
      opts.overwrite = false;

      var content;
      if( opts.content ){
        if( isFunction(opts.content) ){
          content = opts.content;
        } else if( opts.content.text && isFunction(opts.content.text) ){
          content = opts.content.text;
        }

        if( content ){
          opts.content = function(event, api){
            return content.apply( target, [event, api] );
          };
        }
      }

      return opts;
    }

    $$('collection', 'qtip', function( passedOpts ){
      var eles = this;
      var cy = this.cy();
      var container = cy.container();

      if( passedOpts === 'api' ){
        return this.scratch().qtip.api;
      }

      eles.each(function(i, ele){
        var scratch = ele.scratch();
        var qtip = scratch.qtip = scratch.qtip || {};
        var opts = generateOpts( ele, passedOpts );
        var adjNums = opts.position.adjust;


        qtip.$domEle.qtip( opts );
        var qtipApi = qtip.api = qtip.$domEle.qtip('api'); // save api ref
        qtip.$domEle.removeData('qtip'); // remove qtip dom/api ref to be safe

        var updatePosition = function(e){
          var cOff = container.getBoundingClientRect();
          var pos = ele.renderedPosition() || ( e ? e.cyRenderedPosition : undefined );
          if( !pos || pos.x == null || isNaN(pos.x) ){ return; }

          if( opts.position.adjust.cyAdjustToEleBB && ele.isNode() ){
            var my = opts.position.my.toLowerCase();
            var at = opts.position.at.toLowerCase();
            var z = cy.zoom();
            var w = ele.outerWidth() * z;
            var h = ele.outerHeight() * z;

            if( at.match('top') ){
              pos.y -= h/2;
            } else if( at.match('bottom') ){
              pos.y += h/2;
            }

            if( at.match('left') ){
              pos.x -= w/2;
            } else if( at.match('right') ){
              pos.x += w/2;
            }

            if( isNumber(adjNums.x) ){
              pos.x += adjNums.x;
            }

            if( isNumber(adjNums.y) ){
              pos.y += adjNums.y;
            }
          }

          qtipApi.set('position.adjust.x', cOff.left + pos.x + window.pageXOffset);
          qtipApi.set('position.adjust.y', cOff.top + pos.y + window.pageYOffset);
        };
        updatePosition();

        ele.on( opts.show.event, function(e){
          updatePosition(e);

          qtipApi.show();
        } );

        ele.on( opts.hide.event, function(e){
          qtipApi.hide();
        } );

        if( opts.hide.cyViewport ){
          cy.on('viewport', debounce(function(){
            qtipApi.hide();
          }, viewportDebounceRate, { leading: true }) );
        }

        if( opts.position.adjust.cyViewport ){
          cy.on('pan zoom', debounce(function(e){
            updatePosition(e);

            qtipApi.reposition();
          }, viewportDebounceRate, { trailing: true }) );
        }

      });

      return this; // chainability

    });

    $$('core', 'qtip', function( passedOpts ){
      var cy = this;
      var container = cy.container();

      if( passedOpts === 'api' ){
        return this.scratch().qtip.api;
      }

      var scratch = cy.scratch();
      var qtip = scratch.qtip = scratch.qtip || {};
      var opts = generateOpts( cy, passedOpts );


      qtip.$domEle.qtip( opts );
      var qtipApi = qtip.api = qtip.$domEle.qtip('api'); // save api ref
      qtip.$domEle.removeData('qtip'); // remove qtip dom/api ref to be safe

      var updatePosition = function(e){
        var cOff = container.getBoundingClientRect();
        var pos = e.cyRenderedPosition;
        if( !pos || pos.x == null || isNaN(pos.x) ){ return; }

        qtipApi.set('position.adjust.x', cOff.left + pos.x + window.pageXOffset);
        qtipApi.set('position.adjust.y', cOff.top + pos.y + window.pageYOffset);
      };

      cy.on( opts.show.event, function(e){
        if( !opts.show.cyBgOnly || (opts.show.cyBgOnly && e.cyTarget === cy) ){
          updatePosition(e);

          qtipApi.show();
        }
      } );

      cy.on( opts.hide.event, function(e){
        if( !opts.hide.cyBgOnly || (opts.hide.cyBgOnly && e.cyTarget === cy) ){
          qtipApi.hide();
        }
      } );

      if( opts.hide.cyViewport ){
        cy.on('viewport', debounce(function(){
          qtipApi.hide();
        }, viewportDebounceRate, { leading: true }) );
      }

      return this; // chainability

    });

  }

  if( typeof module !== 'undefined' && module.exports ){ // expose as a commonjs module
    module.exports = register;
  }

  if( typeof define !== 'undefined' && define.amd ){ // expose as an amd/requirejs module
    define('cytoscape-qtip', function(){
      return register;
    });
  }

  if( $ && $$ ){
    register( $$, $ );
  }

})(
  typeof jQuery !== 'undefined' ? jQuery : null,
  typeof cytoscape !== 'undefined' ? cytoscape : null
);
