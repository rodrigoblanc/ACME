package es.udc.psi.psproject;

import android.os.Parcel;
import android.os.Parcelable;

public class Favourites implements Parcelable {
    private String name;
    private String number;

    public Favourites (String name, String number){
        this.name = name;
        this.number = number;
    }

    public Favourites(Parcel in) {
        name = in.readString();
        number = in.readString();
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public static final Creator<Favourites> CREATOR = new Creator<Favourites>() {
        @Override
        public Favourites createFromParcel(Parcel in) {
            return new Favourites(in);
        }

        @Override
        public Favourites[] newArray(int size) {
            return new Favourites[size];
        }
    };

    public static Creator<Favourites> getCREATOR() {
        return CREATOR;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(number);
    }


}
