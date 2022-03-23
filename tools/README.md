## Updating Palo Alto Application Definitions

Palo Alto application definitions can be pulled from a firewall or Panorama device via their web APIs.

To pull the definitions you'll need an API key. You can issue a GET request to the keygen API providing a username and password (replace the IP address and `<USERNAME>` and `<PASSWORD>` below):
```
curl -X GET 'https://10.0.0.1/api/?type=keygen&user=<USERNAME>&password=<PASSWORD>'
```

Once you have that API key, you can substitute it in below for `<KEY>`, in the actual config query to pull application definitions:
```
curl -X GET 'https://10.0.0.1/api/?key=<KEY>&type=config&action=get&xpath=/config/predefined/application' > palo_alto_applications.xml
```

Once you have the application definition XML (`palo_alto_applications.xml`), run the script to generate the file needed for Batfish:
```
bazel run //tools:generate_pan_apps
```
