
select * from svc_list;

select * from rsn_list where rsn_list_type = 'BCI' order by rsn_desc;

select * from com_info;

select * from usr_info;

select * from appt_info order by appt_id;

select * from appt_pymt order by appt_id;

select * from inv_info;

select * from exp_info;

select * from appt_info where appt_sts_code = 103 and cncl_ts <= date(now())-interval '2 days';

select * from appt_pymt where pymt_sts_code = 203 and pymt_dt <= date(now())-interval '2 days';

--select setval('com_info_seq', 0);

--select setval('usr_info_seq', 0);

--select setval('exp_info_seq', 0);

--select setval('appt_info_seq', 1000);

--truncate table rsn_list;

-- drop table usr_info;
