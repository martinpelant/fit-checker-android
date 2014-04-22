package cz.mpelant.fitchecker.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
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
public class Subject extends AbstractEntity implements Parcelable {
    public static final String TABLE_NAME = "Subject";
    public static final String NAME = "name";
    public static final String READ = "read";

    @DatabaseField(unique = true, columnName = NAME)
    private String name;

    @DatabaseField(columnName = READ)
    private boolean read;



    public Subject() {
    }

    public Subject(String name) {
        this.name=name;
        read=false;
    }

    public Subject(String name, boolean read) {
        this.name=name;
        this.read=read;
    }


    public boolean isRead() {
        return read;
    }

    public String getName() {
        return name;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Subject(Cursor c) {
        super(c);
        name = c.getString(c.getColumnIndex(NAME));
        read = c.getInt(c.getColumnIndex(READ)) == 1;
    }


    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(NAME, name);
        values.put(READ, read);
        return values;
    }

    public ContentValues getContentValuesReadOnly() {
        ContentValues values = new ContentValues();
        values.put(READ, read);
        return values;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.internalId);
        dest.writeString(this.name);
        dest.writeByte(read ? (byte) 1 : (byte) 0);
    }

    private Subject(Parcel in) {
        this.internalId = in.readLong();
        this.name = in.readString();
        this.read = in.readByte() != 0;
    }

    public static Parcelable.Creator<Subject> CREATOR = new Parcelable.Creator<Subject>() {
        public Subject createFromParcel(Parcel source) {
            return new Subject(source);
        }

        public Subject[] newArray(int size) {
            return new Subject[size];
        }
    };


}
