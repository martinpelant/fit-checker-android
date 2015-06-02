package cz.mpelant.fitchecker.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Exam entity class
 * Created by David Bilik[david.bilik@ackee.cz] on 15. 5. 2014.
 */
@DatabaseTable(tableName = Exam.TABLE_NAME)
public class Exam extends AbstractEntity implements Parcelable {
    public static final String TABLE_NAME = "exam";
    public static final String COL_DATE = "date";
    public static final String COL_ROOM = "room";
    public static final String COL_CAPACITY = "capacity";
    public static final String COL_OCCUPIED = "occupied";
    public static final String COL_SUBJECT = "subject";
    public static final String COL_EXAM_ID = "exam_id";
    public static final String COL_IS_REGISTERED = "is_registered";

    protected static final String TAG = Exam.class.getName();
    public static final String TERM_TYPE_EXAM = "FINAL_EXAM";
    @DatabaseField(columnName = COL_DATE)
    private long dateLong;

    private String date;
    @DatabaseField(columnName = COL_ROOM)
    private String room;
    @DatabaseField(columnName = COL_CAPACITY)
    private int capacity;
    @DatabaseField(columnName = COL_OCCUPIED)
    private int occupied;
    @DatabaseField(columnName = COL_SUBJECT)
    private String subject;
    @DatabaseField(columnName = COL_EXAM_ID, unique = true)
    private String examId;
    @DatabaseField(columnName = COL_IS_REGISTERED)
    private boolean isRegistered;

    private String termType;

    public Exam() {
    }

    public Exam(String date, String room, int capacity, int occupied, String subject, boolean isRegistered) {
        examId = String.valueOf(Math.random());
        this.date = date;
        this.room = room;
        this.capacity = capacity;
        this.occupied = occupied;
        this.subject = subject;
        this.isRegistered = isRegistered;
    }

    public Exam(Cursor cursor) {
        dateLong = cursor.getLong(cursor.getColumnIndex(COL_DATE));
        capacity = cursor.getInt(cursor.getColumnIndex(COL_CAPACITY));
        occupied = cursor.getInt(cursor.getColumnIndex(COL_OCCUPIED));
        room = cursor.getString(cursor.getColumnIndex(COL_ROOM));
        subject = cursor.getString(cursor.getColumnIndex(COL_SUBJECT));
        isRegistered = cursor.getInt(cursor.getColumnIndex(COL_IS_REGISTERED)) == 1;
        examId = cursor.getString(cursor.getColumnIndex(COL_EXAM_ID));
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getOccupied() {
        return occupied;
    }

    public void setOccupied(int occupied) {
        this.occupied = occupied;
    }

    @Override
    public String toString() {
        return "Exam{" +
                "date='" + date + '\'' +
                ", room='" + room + '\'' +
                ", capacity=" + capacity +
                ", occupied=" + occupied +
                '}';
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAPACITY, capacity);
        cv.put(COL_ROOM, room);
        cv.put(COL_OCCUPIED, occupied);
        cv.put(COL_DATE, parseDate(date));
        cv.put(COL_SUBJECT, subject);
        cv.put(COL_EXAM_ID, examId);
        cv.put(COL_IS_REGISTERED, isRegistered);
        return cv;

    }

    private long parseDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        try {
            return sdf.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public String getFormattedDate() {
        String format = "dd. MM. yyyy, HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        return sdf.format(new Date(dateLong));
    }

    public void setExamId(String id) {
        examId = id;
    }

    public void setIsRegistered(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }

    public String getExamId() {
        return examId;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public long getLongDate() {
        return dateLong;
    }

    public String getTermType() {
        return termType;
    }

    public void setTermType(String termType) {
        this.termType = termType;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.dateLong);
        dest.writeString(this.date);
        dest.writeString(this.room);
        dest.writeInt(this.capacity);
        dest.writeInt(this.occupied);
        dest.writeString(this.subject);
        dest.writeString(this.examId);
        dest.writeByte(isRegistered ? (byte) 1 : (byte) 0);
        dest.writeString(this.termType);
        dest.writeLong(this.internalId);
    }

    private Exam(Parcel in) {
        this.dateLong = in.readLong();
        this.date = in.readString();
        this.room = in.readString();
        this.capacity = in.readInt();
        this.occupied = in.readInt();
        this.subject = in.readString();
        this.examId = in.readString();
        this.isRegistered = in.readByte() != 0;
        this.termType = in.readString();
        this.internalId = in.readLong();
    }

    public static Parcelable.Creator<Exam> CREATOR = new Parcelable.Creator<Exam>() {
        public Exam createFromParcel(Parcel source) {
            return new Exam(source);
        }

        public Exam[] newArray(int size) {
            return new Exam[size];
        }
    };
}
