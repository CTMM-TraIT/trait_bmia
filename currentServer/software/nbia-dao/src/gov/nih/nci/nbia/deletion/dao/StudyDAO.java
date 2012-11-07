package gov.nih.nci.nbia.deletion.dao;

import gov.nih.nci.nbia.deletion.DeletionAuditStudyInfo;
import gov.nih.nci.nbia.exception.DataAccessException;

public interface StudyDAO {
	public void removeStudy(Integer studyId) throws DataAccessException;
	public boolean checkStudyNeedToBeRemoved(Integer studyId, Integer count) throws DataAccessException;
	public int getTotalSeriesNumber(Integer studyId) throws DataAccessException;
	public Integer getPatientId(Integer studyId);
	public DeletionAuditStudyInfo getDeletionAuditStudyInfo(Integer studyId);
}
