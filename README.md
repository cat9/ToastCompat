# ToastCompat
ToastCompat for Android 7.1.1, Avoiding Toast BadTokenException , Just what exactly the repo [ToastCompat](https://github.com/drakeet/ToastCompat) do. I name this repo "ToastCompat" just want somebody could be help.


### Why
for more details about [BadTokenException on Toast ](https://github.com/drakeet/ToastCompat)

There is [ToastCompat](https://github.com/drakeet/ToastCompat) do the same thing , why i create this?

[ToastCompat](https://github.com/drakeet/ToastCompat) use a windowmanager wrapper to hook the system windowmanager.
It is a great solution to fix the bug. But when i test in the phone( Nubia Z17S),the Toast can't be show. I can't find the reason for it, 
It is weird，but it do happen. I had reported a bug to the author.

So i have to find another solution to fix the bug `BadTokenException`


### Solution
Just copy [ToastCompat](https://github.com/cat9/ToastCompat/blob/master/app/src/main/java/com/miku/toast/ToastCompat.java) to your project,and use it like `Toast`.

these is the main code to solve the issue:

```java
    @Override
    public void show() {
        if(checkIfNeedToHack()){
            tryToHack();
        }
        super.show();
    }

    protected boolean checkIfNeedToHack(){
        return Build.VERSION.SDK_INT==Build.VERSION_CODES.N_MR1;
    }

    private void tryToHack(){
        try {
            Object mTN=getFieldValue(this,"mTN");
            if(mTN!=null){
                Object rawHandler=getFieldValue(mTN,"mHandler");
                if(rawHandler!=null){
                    setFieldValue(rawHandler,"mCallback",new InternalHandlerCallback((Handler)rawHandler));
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    private class InternalHandlerCallback implements Handler.Callback{
        private final Handler mHandler;

        public InternalHandlerCallback(Handler mHandler) {
            this.mHandler = mHandler;
        }

        @Override
        public boolean handleMessage(Message msg) {
            try {
                mHandler.handleMessage(msg);
            }catch (Throwable e) {
               e.printStackTrace();
            }
            return true;
        }
    }

```
