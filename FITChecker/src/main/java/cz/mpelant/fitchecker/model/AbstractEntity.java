package cz.mpelant.fitchecker.model;

import android.database.Cursor;
import com.j256.ormlite.field.DatabaseField;

/**
 * AbstractEntity.java
 *
 * @author eMan s.r.o.
 * @project chlist-an
 * @package cz.eman.chlist.model
 * @since 3/29/2014
 */
public abstract class AbstractEntity {
    public static final String INTERNAL_ID_COLUMN_NAME = "_id";
    @DatabaseField(generatedId = true, columnName = INTERNAL_ID_COLUMN_NAME)
    protected long internalId;

    public long getId() {
        return internalId;
    }

    public void setId(long internalId) {
        this.internalId = internalId;
    }

    public AbstractEntity(){

    }

    public AbstractEntity(Cursor c){
        internalId=c.getLong(c.getColumnIndex(INTERNAL_ID_COLUMN_NAME));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractEntity that = (AbstractEntity) o;

        return internalId == that.internalId;

    }

    @Override
    public int hashCode() {
        return (int) (internalId ^ (internalId >>> 32));
    }
}
