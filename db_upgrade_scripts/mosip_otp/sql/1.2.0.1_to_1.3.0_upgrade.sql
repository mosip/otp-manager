\c mosip_otp
  
\COPY otp.otp_transaction FROM 'dml/otp_transaction.csv' WITH (FORMAT CSV, HEADER);