parser grammar PaloAlto_profiles;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_profiles: PROFILES sp;

sp
:
    sp_custom_url_category
    | sp_data_filtering
    | sp_data_objects
    | sp_decryption
    | sp_dos_protection
    | sp_file_blocking
    | sp_gtp
    | sp_hip_objects
    | sp_hip_profiles
    | sp_sctp
    | sp_spyware
    | sp_virus
    | sp_vulnerability
    | sp_wildfire_analysis
;

sp_data_filtering: DATA_FILTERING null_rest_of_line;

sp_data_objects: DATA_OBJECTS null_rest_of_line;

sp_decryption: DECRYPTION null_rest_of_line;

sp_dos_protection: DOS_PROTECTION null_rest_of_line;

sp_file_blocking: FILE_BLOCKING null_rest_of_line;

sp_gtp: GTP null_rest_of_line;

sp_hip_objects: HIP_OBJECTS null_rest_of_line;

sp_hip_profiles: HIP_PROFILES null_rest_of_line;

sp_sctp: SCTP null_rest_of_line;

sp_spyware: SPYWARE null_rest_of_line;

sp_virus: VIRUS null_rest_of_line;

sp_vulnerability: VULNERABILITY null_rest_of_line;

sp_wildfire_analysis: WILDFIRE_ANALYSIS null_rest_of_line;

sp_custom_url_category: CUSTOM_URL_CATEGORY custom_url_category_name spc_definition;

spc_definition: spc_description | spc_list | spc_type;

spc_description: DESCRIPTION description = value;

spc_list: LIST list = variable_list;

spc_type: TYPE type = variable;

// Up to 31 characters
custom_url_category_name: variable;
