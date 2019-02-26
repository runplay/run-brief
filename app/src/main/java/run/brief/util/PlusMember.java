package run.brief.util;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import run.brief.R;
import run.brief.b.B;
import run.brief.b.Bgo;
import run.brief.util.pay.PlusMemberFragment;

/**
 * Created by coops on 03/02/15.
 */
public class PlusMember {

    public static View getPlusMemberUpgradeView(final Activity activity, View parentView, View.OnClickListener cancelListner) {
        View upgrade=parentView.findViewById(R.id.upgrade);

        TextView txt1 =(TextView) upgrade.findViewById(R.id.upgrade_head);
        TextView txt2 =(TextView) upgrade.findViewById(R.id.upgrade_blurb);
        TextView txt3 =(TextView) upgrade.findViewById(R.id.upgrade_text_delete);
        TextView txt4 =(TextView) upgrade.findViewById(R.id.upgrade_text_signature);
        TextView txt5 =(TextView) upgrade.findViewById(R.id.upgrade_text_locker);
        TextView btnUpgrade =(TextView) upgrade.findViewById(R.id.upgrade_now);
        btnUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bgo.openFragmentBackStack(activity, PlusMemberFragment.class);
            }
        });
        TextView btnCancel =(TextView) upgrade.findViewById(R.id.upgrade_cancel);
        btnCancel.setOnClickListener(cancelListner);

        B.addStyleBold(txt1);
        B.addStyle(new TextView[]{txt2,txt3,txt4,txt5,btnCancel,btnUpgrade});
        return upgrade;
    }
}
