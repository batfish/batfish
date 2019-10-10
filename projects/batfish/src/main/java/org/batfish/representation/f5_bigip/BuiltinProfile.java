package org.batfish.representation.f5_bigip;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface BuiltinProfile extends Builtin {

  public static @Nullable BuiltinProfile getBuiltinProfile(String name) {
    String unqualifiedName = Builtin.unqualify(name);
    return Stream.<Function<String, ? extends BuiltinProfile>>of(
            BuiltinProfileAnalytics::forName,
            BuiltinProfileCertificateAuthority::forName,
            BuiltinProfileClassification::forName,
            BuiltinProfileClientLdap::forName,
            BuiltinProfileClientSsl::forName,
            BuiltinProfileDhcpv4::forName,
            BuiltinProfileDiameter::forName,
            BuiltinProfileDns::forName,
            BuiltinProfileFastHttp::forName,
            BuiltinProfileFastL4::forName,
            BuiltinProfileFix::forName,
            BuiltinProfileFtp::forName,
            BuiltinProfileGtp::forName,
            BuiltinProfileHtml::forName,
            BuiltinProfileHttp::forName,
            BuiltinProfileHttp2::forName,
            BuiltinProfileHttpCompression::forName,
            BuiltinProfileHttpProxyConnect::forName,
            BuiltinProfileIcap::forName,
            BuiltinProfileIlx::forName,
            BuiltinProfileIpOther::forName,
            BuiltinProfileIpsecAlg::forName,
            BuiltinProfileMapT::forName,
            BuiltinProfileMgtt::forName,
            BuiltinProfileNetflow::forName,
            BuiltinProfileOneConnect::forName,
            BuiltinProfilePcp::forName,
            BuiltinProfilePptp::forName,
            BuiltinProfileQoe::forName,
            BuiltinProfileRadius::forName,
            BuiltinProfileRequestAdapt::forName,
            BuiltinProfileRequestLog::forName,
            BuiltinProfileResponseAdapt::forName,
            BuiltinProfileRewrite::forName,
            BuiltinProfileRtsp::forName,
            BuiltinProfileSctp::forName,
            BuiltinProfileServerLdap::forName,
            BuiltinProfileServerSsl::forName,
            BuiltinProfileSip::forName,
            BuiltinProfileSmtps::forName,
            BuiltinProfileSocks::forName,
            BuiltinProfileSplitSessionClient::forName,
            BuiltinProfileSplitSessionServer::forName,
            BuiltinProfileStatistics::forName,
            BuiltinProfileStream::forName,
            BuiltinProfileTcpAnalytics::forName,
            BuiltinProfileTcp::forName,
            BuiltinProfileTftp::forName,
            BuiltinProfileTrafficAcceleration::forName,
            BuiltinProfileUdp::forName,
            BuiltinProfileWebAcceleration::forName,
            BuiltinProfileWebSocket::forName,
            BuiltinProfileXml::forName)
        .map(f -> f.apply(unqualifiedName))
        .filter(Objects::nonNull)
        .findAny()
        .orElse(null);
  }
}
