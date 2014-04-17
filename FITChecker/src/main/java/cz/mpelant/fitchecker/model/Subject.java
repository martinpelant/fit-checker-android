package cz.mpelant.fitchecker.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Subject.java
 *
 * @author eMan s.r.o.
 * @project FITChecker
 * @package cz.mpelant.fitchecker.entity
 * @since 4/17/2014
 */
@DatabaseTable(tableName = Subject.TABLE_NAME)
public class Subject extends AbstractEntity {
    public static final String TABLE_NAME = "Subject";
    public static final String NAME = "name";
    public static final String READ = "read";

    @DatabaseField(unique = true, columnName = NAME)
    private String name;

    @DatabaseField(columnName = READ)
    private boolean read;


}
