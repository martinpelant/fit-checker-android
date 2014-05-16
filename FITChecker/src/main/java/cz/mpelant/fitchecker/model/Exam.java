package cz.mpelant.fitchecker.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Exam entity class
 * Created by David Bilik[david.bilik@ackee.cz] on 15. 5. 2014.
 */
@DatabaseTable(tableName = Exam.TABLE_NAME)
public class Exam extends AbstractEntity {
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
    @DatabaseField(columnName = COL_DATE, unique = true)
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
    @DatabaseField(columnName = COL_EXAM_ID)
    private String examId;
    @DatabaseField(columnName = COL_IS_REGISTERED)
    private boolean isRegistered;

    private String termType;

    public Exam() {
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            return sdf.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public String getFormattedDate() {
        String format = "dd. MM. yyyy, HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
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
}
