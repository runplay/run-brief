package run.brief.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.HomeFarm;
import run.brief.R;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.beans.Account;
import run.brief.beans.SignatureBean;
import run.brief.email.SignatureDb;
import run.brief.util.PlusMember;

public class EmailEditSignaturesFragment extends BFragment implements BRefreshable {
	private Activity activity;
	private View view;

    private Account useaccount;
	//private TextView lastcollect;
	private TextView btnSave;
    private TextView btnDelete;
    private TextView btnAdd;
    private TextView btnCancel;

    private SignatureAdapter adapter;

    private EditText editSignature;
    private ListView listView;

    private View showView;
    private View editView;
    private View upgradeView;

    private SignatureBean useSignature;
    //private TextView emailMsgOutTitle;



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.activity=getActivity();

        SignatureDb.init(activity);
		view=inflater.inflate(R.layout.signatures,container, false);

        showView = view.findViewById(R.id.signatures_show);
        editView = view.findViewById(R.id.signatures_edit);
        listView = (ListView) view.findViewById(R.id.signatures_list);
        listView.setOnItemClickListener(openListener);

        upgradeView= PlusMember.getPlusMemberUpgradeView(activity, view, cancelListener);

        btnAdd=(TextView) view.findViewById(R.id.signature_new);
        btnAdd.setOnClickListener(addListener);
        btnSave=(TextView) view.findViewById(R.id.signature_save);
        btnDelete=(TextView) view.findViewById(R.id.signature_delete);
        btnCancel=(TextView) view.findViewById(R.id.signature_cancel);

        btnDelete.setOnClickListener(deleteListener);
        btnSave.setOnClickListener(saveListener);

        editSignature=(EditText) view.findViewById(R.id.signature_textedit);

        B.addStyle(new TextView[]{btnAdd,btnSave,btnDelete,editSignature,btnCancel});


		return view;

	}
    @Override
    public void onPause() {
        super.onPause();

    }
	@Override
	public void onResume() {
		super.onResume();
		State.setCurrentSection(State.SECTION_EMAIL_SIGNATURES);

		if(State.hasStateObject(State.SECTION_EMAIL_SIGNATURES,StateObject.LONG_USE_ACCOUNT_ID)) {
			long uaccount = State.getStateObjectLong(State.SECTION_EMAIL_SIGNATURES,StateObject.LONG_USE_ACCOUNT_ID);
			useaccount = AccountsDb.getAccountById(uaccount);

		}
        refresh();
	}


	public void refresh() {
		ActionBarManager.setActionBarBackOnly(activity,activity.getString(R.string.label_signatures), R.menu.accounts,R.color.actionbar_email);
        refreshData();
	}
    public void refreshData() {
        if(HomeFarm.isSubscriber()) {
            adapter = new SignatureAdapter();
            listView.setAdapter(adapter);
        } else {
            upgradeView.setVisibility(View.VISIBLE);
            showView.setVisibility(View.GONE);
            editView.setVisibility(View.GONE);
        }

        //}
    }

    public void showEditView() {
        showView.setVisibility(View.GONE);
        editView.setVisibility(View.VISIBLE);
        if(useSignature==null) {
            editSignature.setText("");
            btnDelete.setVisibility(View.GONE);
            btnCancel.setVisibility(View.VISIBLE);
        } else {
            editSignature.setText(useSignature.getString(SignatureBean.STRING_SIGNATURE));
            btnDelete.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        }

    }

	protected ListView.OnItemClickListener openListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            SignatureBean sig=adapter.getItem(i);
            if(i!=0 && sig!=null) {
                useSignature=sig;
                showEditView();
            }
        }
    };
    protected OnClickListener addListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            useSignature=null;
            showEditView();
        }
    };
    protected OnClickListener cancelListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            showView.setVisibility(View.VISIBLE);
            editView.setVisibility(View.GONE);
            editSignature.setText("");
        }
    };
    protected OnClickListener deleteListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if(useSignature==null) {

            } else {
                //useSignature.setString(SignatureBean.STRING_SIGNATURE,editSignature.getText().toString());
                SignatureDb.deleteSignature(useSignature);
                useSignature=null;
            }
            showView.setVisibility(View.VISIBLE);
            editView.setVisibility(View.GONE);
            editSignature.setText("");
            refreshData();
        }
    };
    protected OnClickListener saveListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if(useSignature==null) {
                SignatureBean newsig = new SignatureBean();
                newsig.setString(SignatureBean.STRING_SIGNATURE,editSignature.getText().toString());
                newsig.setLong(SignatureBean.LONG_ACCOUNT_ID,useaccount.getLong(Account.LONG_ID));
                SignatureDb.addSignature(newsig);
            } else {
                useSignature.setString(SignatureBean.STRING_SIGNATURE,editSignature.getText().toString());
                SignatureDb.updateSignature(useSignature);
                useSignature=null;
            }
            //SignatureDb.
            showView.setVisibility(View.VISIBLE);
            editView.setVisibility(View.GONE);
            editSignature.setText("");
            refreshData();
        }
    };

    private class SignatureAdapter extends BaseAdapter {

        List<SignatureBean> signatures = new ArrayList<SignatureBean>();

        public SignatureAdapter() {
            if(useaccount!=null) {
                signatures= SignatureDb.getSignatures(useaccount);
                boolean markeddef=true;
                for(SignatureBean s: signatures) {
                    if(s.getInt(SignatureBean.INT_USE)>0)
                        markeddef=false;
                }
                SignatureBean defsig = SignatureBean.getDefaultSignature(activity,useaccount);
                if(markeddef)
                    defsig.setInt(SignatureBean.INT_USE,1);
                signatures.add(0,defsig);
            }
        }

        public int getCount() {
            return signatures.size();
        }

        public SignatureBean getItem(int position) {
            return signatures.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null) {
                convertView=activity.getLayoutInflater().inflate(R.layout.signatures_item,null);
            }
            ImageView img = (ImageView) convertView.findViewById(R.id.signature_image);
            TextView txt = (TextView) convertView.findViewById(R.id.signature_text);
            SignatureBean sig = signatures.get(position);
            if(sig!=null) {
                txt.setText(sig.getString(SignatureBean.STRING_SIGNATURE));
                if(sig.getInt(SignatureBean.INT_USE)>0)
                    img.setImageDrawable(activity.getResources().getDrawable(R.drawable.email_signature_tick));
                else
                    img.setImageDrawable(activity.getResources().getDrawable(R.drawable.email_signature));

            }
            return convertView;
        }
    }
}
