package run.brief.util;

import android.app.Activity;
import android.text.Layout;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.URLSpan;
import android.view.MotionEvent;

import run.brief.b.StateObject;
import run.brief.b.Bgo;
import run.brief.b.State;
import run.brief.beans.Email;
import run.brief.beans.Person;
import run.brief.contacts.ContactViewFragment;
import run.brief.contacts.ContactsDb;
import run.brief.email.EmailSendFragment;

public class CustomLinkMovementMethod extends LinkMovementMethod {

    private static Activity movementActivity;

    private static CustomLinkMovementMethod linkMovementMethod = new CustomLinkMovementMethod();

    public boolean onTouchEvent(android.widget.TextView widget, android.text.Spannable buffer, android.view.MotionEvent event) {
        int action = event.getAction();
        boolean hasReturn=false;
        boolean allowsuper=false;
        if (action == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
            if (link.length != 0) {

                String url = link[0].getURL();
                //BLog.e("LINK",url);
                if (url.startsWith("tel")) {
                    String number = url.replace("tel:","");
                    if(number!=null && number.matches("[\\d]+") && number.length()>6) {
                        Person person = ContactsDb.getWithTelephone(movementActivity, number);
                        if (person == null)
                            person = Person.getNewUnknownPerson(movementActivity, number, null);
                        //StateObject sobz = new StateObject(StateObject.STRING_ID, person.getString(Person.LONG_ID) + "");
                        StateObject sobz = new StateObject(StateObject.STRING_BJSON_OBJECT, person.toString());
                        State.addToState(State.SECTION_CONTACTS_ITEM, sobz);
                        Bgo.openFragmentAnimate(movementActivity, ContactViewFragment.class);
                        hasReturn = true;
                    }
                } else if (url.startsWith("mailto")) {
                    String address = url.replace("mailto:","");
                    Email email = new Email();
                    email.setString(Email.STRING_TO,address);
                    State.addToState(State.SECTION_EMAIL_NEW,new StateObject(StateObject.STRING_BJSON_OBJECT,email.toString()));
                    Bgo.openFragmentAnimate(movementActivity,EmailSendFragment.class);
                    hasReturn= true;
                } else {
                    allowsuper=true;
                }

            }
        }
        if(hasReturn)
            return true;
        else if(allowsuper)
            return super.onTouchEvent(widget, buffer, event);
        else
            return false;//super.onTouchEvent(widget, buffer, event);

    }

    public static MovementMethod getInstance(Activity activity) {
        movementActivity=activity;
        return linkMovementMethod;
    }
}