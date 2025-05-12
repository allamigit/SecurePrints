

-- REASON LIST TABLE
CREATE TABLE "secure-prints".rsn_list (
	rsn_id int4 NOT NULL,
	svc_code varchar(10) NOT NULL,
	rsn_code varchar(10) NOT NULL,
	rsn_desc varchar(150) NULL,
	CONSTRAINT pk_rsn_id PRIMARY KEY (rsn_id)
);


-- COMPANY INFORMATION TABLE
CREATE SEQUENCE "secure-prints".com_info_seq
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 10000
	START 1
	CACHE 1
	NO CYCLE;
CREATE TABLE "secure-prints".com_info (
	com_id int4 NOT NULL DEFAULT nextval('com_info_seq'),
	com_name varchar(60) NOT NULL,
	com_address_1 varchar(100) NOT NULL,
	com_address_2 varchar(100) NOT NULL,
	com_phone varchar(20) NOT NULL,
	com_email varchar(80) NOT NULL,
	CONSTRAINT pk_com_id PRIMARY KEY (com_id),
	CONSTRAINT uk_com_name UNIQUE (com_name)
);


-- USER INFORMATION TABLE
CREATE SEQUENCE "secure-prints".usr_info_seq
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 10000
	START 1
	CACHE 1
	NO CYCLE;
CREATE TABLE "secure-prints".usr_info (
	usr_id int4 NOT NULL DEFAULT nextval('usr_info_seq'),
	usr_full_name varchar(60) NOT NULL,
	usr_name varchar(20) NOT NULL,
	usr_paswd varchar(20) NOT NULL,
	usr_sts boolean NOT NULL,
	CONSTRAINT pk_usr_id PRIMARY KEY (usr_id),
	CONSTRAINT uk_usr_name UNIQUE (usr_name)
);


-- CUSTOMER APPOINTMENT INFORMATION TABLE
CREATE SEQUENCE "secure-prints".appt_info_seq
	INCREMENT BY 1
	MINVALUE 1000
	MAXVALUE 1000000
	START 1001
	CACHE 1
	NO CYCLE;
CREATE TABLE "secure-prints".appt_info (
	appt_id varchar(10) NOT NULL,
	cust_first_name varchar(30) NOT NULL,
	cust_last_name varchar(30) NOT NULL,
	cust_email varchar(80) NULL,
	cust_phone varchar(20) NULL,
	svc_code varchar(10) NOT NULL,
	bci_rsn_code varchar(10) NULL,
	bci_rsn_desc varchar(150) NULL,
	fbi_rsn_code varchar(10) NULL,
	fbi_rsn_desc varchar(150) NULL,
	appt_ts timestamp NOT NULL DEFAULT now(),
	appt_sts_code int4 NOT NULL,
	ordr_ts timestamp NOT NULL DEFAULT now(),
	rsch_ts timestamp NULL,
	cncl_ts timestamp NULL,
	cmpl_ts timestamp NULL,
	CONSTRAINT pk_appt_id PRIMARY KEY (appt_id)
);


-- CUSTOMER PAYMENT TABLE
CREATE TABLE "secure-prints".appt_pymt (
	appt_id varchar(10) NOT NULL,
	svc_amt numeric(6, 2) NOT NULL,
	bci_amt numeric(6, 2) NOT NULL,
	pymt_sts_code int4 NOT NULL,
	pymt_method_code int4 NULL,
	pymt_dt date NOT NULL,
	pymt_cmt varchar(100) NULL,
	pymt_rcncl_dt date NULL,
	CONSTRAINT pk_pymt_appt_id PRIMARY KEY (appt_id)
);


-- INSERT DATA
insert into com_info values (default, 'Secure Prints LLC', '1105 Schrock Rd, STE 130C', 'Columbus, Ohio 43229', '(713) 815-8120', 'secureprintscan@gmail.com');

insert into usr_info values (default, 'Mawj Al-Lami', 'admin', 'admin', true);

