

select * from rsn_list where rsn_list_type = 'BCI' order by rsn_desc;

select * from com_info;

select * from usr_info;

select * from appt_info order by appt_id;

select * from appt_pymt order by appt_id;

select * from inv_info;

select * from exp_info order by exp_ref_no;

--select * from appt_info where appt_sts_code = 103 and cncl_ts <= date(now())-interval '2 days';

--select * from appt_pymt where pymt_sts_code = 203 and pymt_dt <= date(now())-interval '2 days';

--select setval('com_info_seq', 0);

--select setval('usr_info_seq', 0);

--select setval('exp_info_seq', 0);

--select setval('appt_info_seq', 1234);

--truncate table rsn_list;

-- drop table usr_info;

--select nextval('appt_info_seq');

--update exp_info set exp_rcncl_dt = (select pymt_rcncl_dt from appt_pymt where appt_id = '1277') where exp_ref_no = 'ApptID-1277';

update appt_pymt set bci_amt = -1 * bci_amt; --where appt_id like '%-R';

--update appt_info set usr_ip = '3JMR+UCtEi5q83nFPAWWuA==';

--update exp_info set exp_desc = '' where exp_desc is not null;
--update appt_pymt set pymt_cmt = '' where pymt_cmt is null;
--alter table appt_info add column usr_ip varchar(20) null;

--  Remote: 24.31.160.60, Local: 172.31.15.95 - ec2-user
--  Remote: 174.207.97.255, Local: 172.31.15.95 - ec2-user
--  admin = MaDBdOUgy0x+gEIsHDVqgQ==
--  0:0:0:0:0:0:0:1 = 3JMR+UCtEi5q83nFPAWWuA==

select * from appt_info where cust_first_name = '3PnYiK7w+C8rIAMsjWFI8Q==' and cust_last_name = '3PnYiK7w+C8rIAMsjWFI8Q==' and appt_sts_code between 101 and 102;

select exp_payee_name from exp_info where lower(exp_payee_name) = 'landlord';



INSERT INTO appt_info
(appt_id, cust_first_name, cust_last_name, cust_email, cust_phone, svc_code, bci_rsn_code, bci_rsn_desc, fbi_rsn_code, fbi_rsn_desc, appt_ts, appt_sts_code, ordr_ts, rsch_ts, cncl_ts, cmpl_ts)
VALUES('1234', 'D3XgacuQjh+8C/duLOJ8DA==', 'fY4NUfulFb90DM13+2KFbg==', 'Kmp9TSMaG0mQ2BZJZgxGAA==', 'Kmp9TSMaG0mQ2BZJZgxGAA==', 'BCI', '4723 09', NULL, '', NULL, '2025-07-18 13:15:00.000', 101, '2025-07-15 07:58:32.338', NULL, NULL, NULL);

INSERT INTO appt_pymt
(appt_id, svc_amt, bci_amt, pymt_sts_code, pymt_method_code, pymt_dt, pymt_cmt, pymt_rcncl_dt, pymt_updt)
VALUES('1234', 38.00, 22.00, 201, NULL, '2025-07-18', NULL, NULL, true);

