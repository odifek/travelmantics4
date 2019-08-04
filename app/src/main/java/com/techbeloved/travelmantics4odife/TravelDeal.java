package com.techbeloved.travelmantics4odife;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class TravelDeal implements Parcelable {
    private String id;
    private String title;
    private String description;
    private String price;
    private String imageUrl;
    private String imageName;

    public TravelDeal() {
    }

    public TravelDeal(String title, String description, String price, String imageUrl, String imageName) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.imageName = imageName;
    }

    protected TravelDeal(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        price = in.readString();
        imageUrl = in.readString();
        imageName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(price);
        dest.writeString(imageUrl);
        dest.writeString(imageName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TravelDeal> CREATOR = new Creator<TravelDeal>() {
        @Override
        public TravelDeal createFromParcel(Parcel in) {
            return new TravelDeal(in);
        }

        @Override
        public TravelDeal[] newArray(int size) {
            return new TravelDeal[size];
        }
    };

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImageName() {
        return imageName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TravelDeal{");
        sb.append("id='").append(id).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", price='").append(price).append('\'');
        sb.append(", imageUrl='").append(imageUrl).append('\'');
        sb.append(", imageName='").append(imageName).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TravelDeal that = (TravelDeal) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(price, that.price) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(imageName, that.imageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, price, imageUrl, imageName);
    }
}
