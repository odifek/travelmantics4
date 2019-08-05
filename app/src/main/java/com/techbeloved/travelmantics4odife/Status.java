package com.techbeloved.travelmantics4odife;

public abstract class Status {

    public abstract boolean saved();
    public abstract boolean isDefault();
    public abstract boolean error();

    public abstract Throwable getError();

    public static Status defaultVal() {
        return new Status() {

            @Override
            public boolean saved() {
                return false;
            }

            @Override
            public boolean isDefault() {
                return true;
            }

            @Override
            public boolean error() {
                return false;
            }

            @Override
            public Throwable getError() {
                return null;
            }
        };
    }

    public static Status savedSuccess() {
        return new Status() {

            @Override
            public boolean saved() {
                return true;
            }

            @Override
            public boolean isDefault() {
                return false;
            }

            @Override
            public boolean error() {
                return false;
            }

            @Override
            public Throwable getError() {
                return null;
            }
        };
    }

    public static Status errorSaving(Throwable throwable) {
        return new Status() {

            @Override
            public boolean saved() {
                return false;
            }

            @Override
            public boolean isDefault() {
                return false;
            }

            @Override
            public boolean error() {

                return true;
            }

            @Override
            public Throwable getError() {
                return throwable;
            }
        };
    }

}
