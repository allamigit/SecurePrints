
-- REASON LIST TABLE
CREATE TABLE "secure-prints".rsn_list (
	rsn_id int4 NOT NULL,
	rsn_list_type varchar(3) NOT NULL,
	rsn_code varchar(10) NOT NULL,
	rsn_text varchar(150) NULL,
	CONSTRAINT pk_rsn_id PRIMARY KEY (rsn_id)
);


-- CUSTOMER INFORMATION TABLE
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
	bci_rsn_text varchar(150) NULL,
	fbi_rsn_code varchar(10) NULL,
	fbi_rsn_text varchar(150) NULL,
	appt_ts timestamp NOT NULL DEFAULT now(),
	svc_amt numeric(6,2) NOT NULL,
	appt_sts_code int4 NOT NULL,
	ordr_ts timestamp NOT NULL DEFAULT now(),
	rsch_ts timestamp NULL,
	cncl_ts timestamp NULL,
	cmpl_ts timestamp NULL,
	CONSTRAINT pk_appt_id PRIMARY KEY (appt_id)
);


-- APPOINTMENT PAYMENT TABLE
CREATE TABLE "secure-prints".appt_pymt (
	appt_id int8 NOT NULL,
	svc_code varchar(10) NOT NULL,
	svc_amt numeric(6,2) NOT NULL,
	pymt_type int4 NOT NULL,
	pymt_method int4 NOT NULL,
	pymt_dt date NOT NULL,
	pymt_cmt varchar(100) NULL,
	pymt_rcncl_dt date NULL,
	CONSTRAINT pk_pymt_appt_id PRIMARY KEY (appt_id)
);


-- INSERT DATA
-- RSN_LIST
insert into rsn_list values (1, 'BCI', '4768 06', 'A controlling person of an appraisal management company');
insert into rsn_list values (2, 'BCI', '4701 08', 'Accountancy Board license applicants');
insert into rsn_list values (3, 'BCI', 'NO ORC', '');

insert into rsn_list values (4, 'FBI', '3769 03', 'Ohio Racing Commission – Horse Racing applicants');
insert into rsn_list values (5, 'FBI', '113 041', 'Ohio Treasurer of State Employees, applicants');
insert into rsn_list values (6, 'FBI', 'NO ORC', '');



