package run.brief.contacts;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import run.brief.b.B;
import run.brief.R;
import run.brief.b.State;
import run.brief.beans.Person;

public class ContactsAdapter extends BaseAdapter {
 
    private Activity activity;
    private List<Person> data=new ArrayList();
    private LayoutInflater inflater=null;
    
    //public static HashMap<String,PersonSlim> selectedPersonSlims=new HashMap<String,PersonSlim>();
    
    private ImageView ivFlip;
    private Animation animation1;
    private Animation animation2;
    private ContactsHomeFragment parentFragment;
    private static int MODE=State.CONTACT_MODE_VIEW;

    public void clearData() {
        data.clear();
        rows.clear();
    }

    public ContactsAdapter(Activity a, List<Person> usedata) {
        activity = a;
        
        MODE=State.getContactsMode();
        data.clear();
        for(Person p: usedata) {
        	if(MODE==State.CONTACT_MODE_SELECT_EMAIL && p.getJSONArray(Person.JSONARRAY_EMAIL).length()>0)
        		data.add(p);
        	else if(p.getJSONArray(Person.JSONARRAY_PHONE).length()>0)
        		data.add(p);
        		
        }
        //this.data=data;
        
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		animation1 = AnimationUtils.loadAnimation(activity, R.anim.to_middle);
		animation2 = AnimationUtils.loadAnimation(activity, R.anim.from_middle);
    }

    public static class Row extends Person {}

    public static final class Section extends Row {
        public final String text;

        public Section(String text, Person person) {
            super();
            this.text = text;
        }
    }

    public static final class Item extends Row {
        public final String text;

        public Item(String text, Person person) {
            this.bean=person.getBean();
            this.text = text;
        }
    }

    private List<Row> rows;

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof Section) {
            return 1;
        } else {
            return 0;
        }
    }


    public void setRefeshableParent(ContactsHomeFragment fragment) {
    	this.parentFragment=fragment;
    }

    public List<Person> getData() {
        return data;
    }
 /*
    public static void setSelectedPersonSlims(HashMap<String,PersonSlim> tos) {
    	selectedPersonSlims=tos;
    }


       public int getCount() {
        return data.size();
    }
    public Object getItem(int position) {
        return position;
    }
 */
    public long getItemId(int position) {
        return position;
    }
    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public Row getItem(int position) {
        return rows.get(position);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        //BLog.e("CAD","getview: "+position);

        View view=convertView;

        if (getItemViewType(position) == 0) { // Item
            view=getContactView(position,convertView,parent);

        } else { // Section
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = (LinearLayout) inflater.inflate(R.layout.contacts_alphabet_row_section, parent, false);
            }

            Section section = (Section) getItem(position);
            TextView textView = (TextView) view.findViewById(R.id.textView1);
            B.addStyle(textView);
            textView.setText(section.text);
        }

        return view;
    }
    public View getContactView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;

        ViewHolder holder;
        if (vi == null) {
            vi = inflater.inflate(R.layout.contacts_names, null);
            //convertView = inflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.name = (TextView) vi.findViewById(R.id.contact_name);
            holder.selectBox = (ImageView) vi.findViewById(R.id.contact_image);
            vi.setTag(holder);
        }
        holder = (ViewHolder) vi.getTag();

        //TextView name = (TextView)vi.findViewById(R.id.contact_name);
        //bRoundedImageView image = (bRoundedImageView)vi.findViewById(R.id.contact_image);
        TextView number = (TextView) vi.findViewById(R.id.contact_number);
        TextView email = (TextView) vi.findViewById(R.id.contact_email);
        ImageView selected = (ImageView) vi.findViewById(R.id.contact_image_selected);

        B.addStyle(new TextView[]{number, email, holder.name});

        selected.setVisibility(View.GONE);

        View imageAlphaHolder = (View) vi.findViewById(R.id.contact_thumbnail_alpha);
        email.setText("");
        number.setText("");


        holder.selectBox.setTag("" + position);

        Person c = rows.get(position);
        if (c != null) {
            holder.name.setText(c.getString(Person.STRING_NAME));

            if (MODE != State.CONTACT_MODE_VIEW) {
                vi.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //(bRoundedImageView) vi.findViewById(R.id.contact_image);
                        View selectView = v.findViewById(R.id.contact_image);
                        if (selectView != null) {
                            Person f = rows.get(Integer.parseInt(selectView.getTag().toString()));
                            ImageView selected = (ImageView) v.findViewById(R.id.contact_image_selected);
                            if (ContactsSelectedClipboard.get(f.getString(Person.STRING_PERSON_ID)) != null) {

                                ContactsSelectedClipboard.removePerson(f);
                                //selected.setVisibility(View.GONE);
                                notifyDataSetChanged();
                                setAnimListners(f);
                                if (parentFragment != null) {
                                    //parentFragment
                                    //parentFragment.setTos(selectedPersonSlims);
                                    parentFragment.refresh();
                                }
                            } else {

                                ContactsSelectedClipboard.addPerson(f);
                                //File fi=new File(f.file);
                                //if(fi.exists() && !fi.isDirectory()) {
                                ivFlip = (ImageView) selectView;
                                ivFlip.clearAnimation();
                                ivFlip.setAnimation(animation1);
                                ivFlip.startAnimation(animation1);
                                //selected.setVisibility(View.VISIBLE);
                                setAnimListners(f);
                                if (parentFragment != null) {
                                    //parentFragment
                                    //parentFragment.setTos(selectedPersonSlims);
                                    parentFragment.refresh();
                                }
                            }
                        }
                    }

                });

            }

            if (ContactsSelectedClipboard.get(c.getString(Person.STRING_PERSON_ID)) != null) {
                //holder.selectBox.setImageResource(R.drawable.cb_checked);
                vi.setBackgroundColor(activity.getResources().getColor(R.color.actionbar_basic));
                selected.setVisibility(View.VISIBLE);
                imageAlphaHolder.setAlpha(0.3F);
                //holder.selectBox.setChecked(true);

            } else {
                vi.setBackgroundColor(activity.getResources().getColor(android.R.color.transparent));

                //holder.selectBox.setImageResource(R.drawable.cb_unchecked);
                selected.setVisibility(View.GONE);
                //holder.selectBox.setChecked(false);
                imageAlphaHolder.setAlpha(1F);
            }
            Bitmap b = c.getThumbnail(activity);
            holder.selectBox.setImageBitmap(b);
            String mainnum = (String) c.getJSONArray(Person.JSONARRAY_PHONE).get(0);

            if (MODE == State.CONTACT_MODE_SELECT_SMS || MODE == State.CONTACT_MODE_VIEW) {
                if (!mainnum.isEmpty()) {
                    number.setText(mainnum);
                    //butSms.setVisibility(View.VISIBLE);
                    if (State.getContactsMode() == State.CONTACT_MODE_VIEW) {
                        //butPhone.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                number.setText("");
            }


            if (MODE == State.CONTACT_MODE_SELECT_EMAIL || MODE == State.CONTACT_MODE_VIEW) {
                if (c.hasEmail()) {
                    String mainemail = (String) c.getJSONArray(Person.JSONARRAY_EMAIL).get(0);
                    if (!mainemail.isEmpty()) {
                        email.setText(mainemail);
                        //butEmail.setVisibility(View.VISIBLE);

                    }
                }
            }


        }
        return vi;
    }
    private void setAnimListners(final Person curPerson) {
        AnimationListener animListner;
        animListner = new AnimationListener() {
 
            @Override
            public void onAnimationStart(Animation animation) {
                if (animation == animation1) {
//
                    ivFlip.clearAnimation();
                    ivFlip.setAnimation(animation2);
                    ivFlip.startAnimation(animation2);
                } else {
                    //curMail.setChecked(!curMail.isChecked());
                    //setCount();
                    setActionMode();
                }
            }
            // Show/Hide action mode
            private void setActionMode() {

            	notifyDataSetChanged();
            }
 
            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub
 
            }
 
            @Override
            public void onAnimationEnd(Animation arg0) {
                // TODO Auto-generated method stub
 
            }
        };
 
        animation1.setAnimationListener(animListner);
        animation2.setAnimationListener(animListner);
 
    }
    private static class ViewHolder {
        TextView name;
        ImageView selectBox;
    }
}
