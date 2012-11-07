#MAKE SURE YOU DONT LEAVE A DANGLING STUDY!!!



create temporary table image_temp as 
  select image_pk_id 
  from general_image 
  where general_series_pk_id in (1342374355,1342374356,1342374357,1342374358);


delete c
from ct_image c, image_temp i
where c.image_pk_id = i.image_pk_id;

delete q
from qa_status_history q, image_temp i
where q.general_image_pk_id = i.image_pk_id;

delete g
from group9_dicom_tags g, image_temp i
where g.image_pk_id = i.image_pk_id;

delete gi
from general_image gi
where gi.general_series_pk_id in (1342374355,1342374356,1342374357,1342374358);

delete a
from annotation a  
where a.general_series_pk_id in (1342374355,1342374356,1342374357,1342374358);

delete 
from general_series 
where general_series_pk_id in (1342374355,1342374356,1342374357,1342374358);



drop table image_temp;
