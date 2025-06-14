

-- REASON LIST TABLE
CREATE TABLE rsn_list (
	rsn_id int4 NOT NULL,
	svc_code varchar(10) NOT NULL,
	rsn_code varchar(10) NOT NULL,
	rsn_desc varchar(150) NULL,
	CONSTRAINT pk_rsn_id PRIMARY KEY (rsn_id)
);


-- COMPANY INFORMATION TABLE
CREATE SEQUENCE com_info_seq
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 10000
	START 1
	CACHE 1
	NO CYCLE;
CREATE TABLE com_info (
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
CREATE TABLE usr_info (
	usr_name varchar(20) NOT NULL,
	usr_paswd text NOT NULL,
	usr_full_name varchar(60) NOT NULL,
	usr_sts boolean NOT NULL,
	CONSTRAINT pk_usr_name PRIMARY KEY (usr_name)
);


-- CUSTOMER APPOINTMENT INFORMATION TABLE
CREATE SEQUENCE appt_info_seq
	INCREMENT BY 1
	MINVALUE 1000
	MAXVALUE 1000000
	START 1001
	CACHE 1
	NO CYCLE;
CREATE TABLE appt_info (
	appt_id varchar(10) NOT NULL,
	cust_first_name text NOT NULL,
	cust_last_name text NOT NULL,
	cust_email text NULL,
	cust_phone varchar(30) NULL,
	svc_code varchar(10) NOT NULL,
	bci_rsn_code varchar(20) NULL,
	bci_rsn_desc varchar(150) NULL,
	fbi_rsn_code varchar(20) NULL,
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
CREATE TABLE appt_pymt (
	appt_id varchar(10) NOT NULL,
	svc_amt numeric(6, 2) NOT NULL,
	bci_amt numeric(6, 2) NOT NULL,
	pymt_sts_code int4 NOT NULL,
	pymt_method_code int4 NULL,
	pymt_dt date NOT NULL,
	pymt_cmt varchar(100) NULL,
	pymt_rcncl_dt date NULL,
	pymt_updt boolean NOT NULL,
	CONSTRAINT pk_pymt_appt_id PRIMARY KEY (appt_id)
);


-- INVOICE INFORMATION TABLE
CREATE TABLE inv_info (
	inv_no varchar(15) NOT NULL,
	inv_payee_name varchar(80) NOT NULL,
	inv_dt date NOT NULL,
	inv_due_dt date NOT NULL,
	inv_amt numeric(6, 2) NOT NULL,
	inv_pymt_sts_code int4 NOT NULL,
	inv_pymt_dt date NULL,
	inv_pymt_method_code int4 NULL,
	inv_cmt varchar(100) NULL,
	inv_doc_file_name varchar(60) NULL,
	inv_rcncl_dt date NULL,
	CONSTRAINT pk_inv_no PRIMARY KEY (inv_no)
);


-- EXPENSE INFORMATION TABLE
CREATE SEQUENCE exp_info_seq
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 10000
	START 1
	CACHE 1
	NO CYCLE;
CREATE TABLE exp_info (
	exp_id int8 NOT NULL DEFAULT nextval('exp_info_seq'),
	exp_payee_name varchar(80) NOT NULL,
	exp_ref_no varchar(15) NOT NULL,
	exp_ref_dt date NOT NULL,
	exp_cat_code int4 NOT NULL,
	exp_sub_cat_code int4 NOT NULL,
	exp_desc varchar(100) NULL,
	exp_amt numeric(6, 2) NOT NULL,
	exp_pymt_sts_code int4 NOT NULL,
	exp_pymt_dt date NULL,
	exp_pymt_method_code int4 NULL,
	exp_doc_file_name varchar(60) NULL,
	exp_rcncl_dt date NULL,
	exp_updt boolean NOT NULL,
	CONSTRAINT pk_exp_id PRIMARY KEY (exp_id),
	CONSTRAINT uk_exp_ref_no UNIQUE (exp_ref_no)
);
CREATE INDEX idx_exp_ref_no ON exp_info(exp_ref_no);


-- INSERT DATA
insert into com_info values (default, 'Secure Prints LLC', '1105 Schrock Rd, STE 130C', 'Columbus, Ohio 43229', '(713) 815-8120', 'secureprintscan@gmail.com');

insert into usr_info values ('admin', '$2a$10$zNbq8q1.5SbOyyTyJ/tlb..MVtOp4K5a0GYzN6nDiUqHv5CmkwFz.', 'Mawj Al-Lami', true);
insert into usr_info values ('admin2', '$2a$10$kJdaPbXbet.a6z4tKT270uOMCU9zVO7LnQnJB0lbFQ41qtjOcnb2W', 'Mohammad Al-Lami', true);

