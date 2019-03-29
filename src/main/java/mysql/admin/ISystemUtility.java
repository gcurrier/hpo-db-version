package mysql.admin;

/**
 * Interface for common methods
 *
 * @author curg
 * @version 2019-03-28
 */
public interface ISystemUtility {

    void doCommit();
    void doRollback();
    void dropDB();
    void createDB();
}
