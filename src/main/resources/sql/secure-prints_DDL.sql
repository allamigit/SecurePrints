
-- DATA TABLES

CREATE TABLE "secure-prints".svc_list (
	svc_code varchar(10) NOT NULL,
	svc_name varchar(40) NOT NULL,
	svc_price numeric(6,2) NOT NULL,
	CONSTRAINT pk_svc_code PRIMARY KEY (svc_code)
);

CREATE SEQUENCE "secure-prints".rsn_list_seq
	INCREMENT BY 1
	MINVALUE 100
	MAXVALUE 220
	START 101
	CACHE 1
	NO CYCLE;
CREATE TABLE "secure-prints".rsn_list (
	rsn_id int4 NOT NULL DEFAULT nextval('rsn_list_seq'),
	rsn_type varchar(3) NOT NULL,
	rsn_text varchar(150) NOT NULL,
	rsn_code varchar(10) NOT NULL,
	CONSTRAINT pk_rsn_id PRIMARY KEY (rsn_id)
);
CREATE INDEX idx_rsn_list_rsn_type ON "secure-prints".rsn_list(rsn_type);
CREATE INDEX idx_rsn_list_rsn_code ON "secure-prints".rsn_list(rsn_code);


-- CUSTOMER TABLE

CREATE SEQUENCE "secure-prints".appt_info_seq
	INCREMENT BY 1
	MINVALUE 1000
	MAXVALUE 1000000
	START 1001
	CACHE 1
	NO CYCLE;
CREATE TABLE "secure-prints".appt_info (
	appt_id int8 NOT NULL,
	cust_first_name varchar(30) NOT NULL,
	cust_last_name varchar(30) NOT NULL,
	cust_email varchar(80) NULL,
	cust_phone varchar(20) NULL,
	svc_code varchar(10) NOT NULL,
	bci_rsn_code varchar(10) NULL,
	fbi_rsn_code varchar(10) NULL,
	appt_ts timestamp NOT NULL DEFAULT now(),
	svc_amt numeric(6,2) NOT NULL,
	appt_sts int4 NOT NULL,
	ordr_ts timestamp NOT NULL DEFAULT now(),
	rsch_ts timestamp NULL,
	cncl_ts timestamp NULL,
	cmpl_ts timestamp NULL,
	CONSTRAINT pk_appt_id PRIMARY KEY (appt_id)
);

-- INSERT DATA

-- SVC_LIST
insert into svc_list values ('BCI', 'BCI Background Check', 38);
insert into svc_list values ('FBI', 'FBI Background Check', 48);
insert into svc_list values ('BCI_FBI', 'BCI & FBI Background Check', 68);

-- RSN_LIST
insert into rsn_list (rsn_type, rsn_text, rsn_code) values ('BCI', 'A controlling person of an appraisal management company', '4768 06');
insert into rsn_list (rsn_type, rsn_text, rsn_code) values ('BCI', 'Accountancy Board license applicants', '4701 08');

insert into rsn_list (rsn_type, rsn_text, rsn_code) values ('FBI', 'Ohio Racing Commission – Horse Racing applicants', '3769 03');
insert into rsn_list (rsn_type, rsn_text, rsn_code) values ('FBI', 'Ohio Treasurer of State Employees, applicants', '113 041');



