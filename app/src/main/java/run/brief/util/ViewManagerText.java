package run.brief.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import run.brief.b.BRefreshable;
import run.brief.b.B;
import run.brief.b.Bgo;
import run.brief.b.Device;
import run.brief.R;
import run.brief.b.State;
import run.brief.b.bEditText;
import run.brief.beans.BriefSettings;
import run.brief.util.eicon.EmoticonsGridAdapter.KeyClickListener;
import run.brief.util.eicon.EmoticonsPagerAdapter;

public class ViewManagerText {

    private static final ViewManagerText VMT = new ViewManagerText();

	private Activity activity;
	private static EditText editField;

    private KeyClickListener listen;
	private static PopupWindow popupWindow;
	private static View popUpView;
	private static LinearLayout emoticonsCover;
	private static EmoticonsPagerAdapter adapter;
	private static Drawable[] demoticons;
	private static ViewPager pager;
	private static EditText emoFaces;
	private static EditText emoNature;
	private static EditText emoItems;
	private static EditText emoEngineering;
	private static EditText emoMisc;
	private static ImageButton emoticonAll;
    private static ImageView useSmsSmile;
    private BRefreshable refreshable;
    //Animation animateAlpha = null;
	//TextView textField
    public static final String EMO_TICK_BOX="☑ ";
	
	public static PopupWindow getPopupWindow() {
		return popupWindow;
	}

    public void manageEditText(final Activity activity, bEditText editTextView,KeyClickListener listen,BRefreshable refreshableAfterTextChange) {

        manageEditText(activity, editTextView,listen);
        VMT.refreshable=refreshableAfterTextChange;

    }
    /*
    public void refreshText(Activity activity, SpannableString spannable) {
        //editField.removeOnAttachStateChangeListener(att);
        //editField.removeTextChangedListener(tw);
        addSmiles(activity, spannable);
        //editField.addTextChangedListener(tw);
        //editField.addOnAttachStateChangeListener(att);
    }
    */
	public void manageEditText(final Activity activity, bEditText editTextView,KeyClickListener listen) {
		
		if(State.getSettings().getBoolean(BriefSettings.BOOL_USE_EMOTICONS).booleanValue()) {
			this.activity=activity;
			editField=editTextView;
			applyEmoticonsToEditText();
		}
        popUpView = activity.getLayoutInflater().inflate(R.layout.emoticons_popup, null);
    	
    	emoticonsCover = (LinearLayout) activity.findViewById(R.id.footer_for_emoticons);
    	
		popupWindow = new PopupWindow(popUpView, LayoutParams.MATCH_PARENT,
				(int) Device.getKeyboardHeight(activity), false);
		enablePopUpView(activity,editTextView,listen);
        VMT.refreshable=null;

        editField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

            }
        });
        /*
        enable.postDelayed(new Runnable() {
            @Override
            public void run() {
                generateEmojiBitmaps(activity);
            }
        },600);
        */
        //animateAlpha= AnimationUtils.loadAnimation(activity, R.anim.alpha_flash);
	}
	public static void manageTextView(Activity activity, TextView textField) {
		
		if(State.getSettings().getBoolean(BriefSettings.BOOL_USE_EMOTICONS).booleanValue()) {
			//textField.set
			Spannable str = new SpannableString(textField.getText());
			addSmiles(activity,str);
			textField.setText(str);
			textField.refreshDrawableState();
		}
        VMT.refreshable=null;
	}
	
	
	public static List<List<String>> getEmoji() {
		return emoji;
	}
    public static List<String> getEmoticonsDefaults() {
        return emoticonsDefaults;
    }
	
	//  Emoticons edittext functions
	private static final Factory spannableFactory = Spannable.Factory.getInstance();
	private static final Map<Pattern, Integer> emoticons = new HashMap<Pattern, Integer>();
    private static final List<String> emoticonsDefaults=new ArrayList<String>();
	
	private static final HashMap<String, Integer> emoticonslookup = new HashMap<String, Integer>();
	
	//private static final List<String> emojiAll = new ArrayList<String>();
    private static final List<String> splitpoints = new ArrayList<String>();
    private static final List<List<String>> emoji = new ArrayList<List<String>>();
    private static final Map<Pattern, Integer> emojiiLookup = new HashMap<Pattern, Integer>();

    //public static Map<String,Bitmap> emojiBitmaps = new HashMap<String,Bitmap>();

    private static Handler loadBitmaps = new Handler();
	
	static {
		List<String> tmpemoji = new ArrayList<String>();
		// get from http://getemoji.com/
		//tmpemoji.add("😄 😃 😀 😊 ☺");
         splitpoints.add("🐶");
         splitpoints.add("🍵");
         splitpoints.add("🏠");
         splitpoints.add("🇯🇵");
         // faces
		tmpemoji.add("😄 😃 😀 😊 ☺ 😉 😍 😘 😚 😗 😙 😜 😝 😛 😳 😁 😔 😌 😒 😞 😣 😢 😂 😭 😪 😥 😰 😅 😓 😩 😫 😨 😱 😠 😡 😤 😖 😆 😋 😷 😎 😴 😵 😲 😟 😦 😧 😈 👿 😮 😬 😐 😕 😯 😶 😇 😏 😑 👲 👳 👮 👷 💂 👶 👦 👧 👨 👩 👴 👵 👱 👼 👸 😺 😸 😻 😽 😼 🙀 😿 😹 😾 👹 👺 🙈 🙉 🙊 💀 👽 💩");
		
		// misc
		tmpemoji.add("🔥 ✨ 🌟 💫 💥 💢 💦 💧 💤 💨 👂 👀 👃 👅 👄 👍 👎 👌 👊 ✊ ✌ 👋 ✋ 👐 👆 👇 👉 👈 🙌 🙏 ☝ 👏 💪 🚶 🏃 💃 👫 👪 👬 👭 💏 💑 👯 🙆 🙅 💁 🙋 💆 💇 💅 👰 🙎 🙍 🙇");
		//hearts
		tmpemoji.add("💛 💙 💜 💚 ❤ 💔 💗 💓 💕 💖 💞 💘 💌 💋 💍 💎 👤 👥 💬 👣 💭");
		
		//BLog.e("OUT", "pos: "+tmpemoji.size());
		
		// animals
		tmpemoji.add("🐶 🐺 🐱 🐭 🐹 🐰 🐸 🐯 🐨 🐻 🐷 🐽 🐮 🐗 🐵 🐒 🐴 🐑 🐘 🐼 🐧 🐦 🐤 🐥 🐣 🐔 🐍 🐢 🐛 🐝 🐜 🐞 🐌 🐙 🐚 🐠 🐟 🐬 🐳 🐋 🐄 🐏 🐀 🐃 🐅 🐇 🐉 🐎 🐐 🐓 🐕 🐖 🐁 🐂 🐲 🐡 🐊 🐫 🐪 🐆 🐈 🐩 🐾");
		// plants flowers
		tmpemoji.add("💐 🌸 🌷 🍀 🌹 🌻 🌺 🍁 🍃 🍂 🌿 🌾 🍄 🌵 🌴 🌲 🌳 🌰 🌱 🌼 ");
		// science and weather
		tmpemoji.add("🌐 🌞 🌝 🌚 🌑 🌒 🌓 🌔 🌕 🌖 🌗 🌘 🌜 🌛 🌙 🌍 🌎 🌏 🌋 🌌 🌠 ⭐ ☀ ⛅ ☁ ⚡ ☔ ❄ ⛄ 🌀 🌁 🌈 🌊");
		
		//BLog.e("OUT", "pos: "+tmpemoji.size());
				
		// food and drink
		tmpemoji.add("☕ 🍵 🍶 🍼 🍺 🍻 🍸 🍹 🍷 🍴 🍕 🍔 🍟 🍗 🍖 🍝 🍛 🍤 🍱 🍣 🍥 🍙 🍘 🍚 🍜 🍲 🍢 🍡 🍳 🍞 🍩 🍮 🍦 🍨 🍧 🎂 🍰 🍪 🍫 🍬 🍭 🍯");
		// fruit and veg
		tmpemoji.add("🍎 🍏 🍊 🍋 🍒 🍇 🍉 🍓 🍑 🍈 🍌 🍐 🍍 🍠 🍆 🍅 🌽");
		//Clothes
		tmpemoji.add("🎩 👑 👒 👟 👞 👡 👠 👢 👕 👔 👚 👗 🎽 👖 👘 👙 💼 👜 👝 👛 👓 🎀 🌂 💄");
		// music and arts
		tmpemoji.add("🎨 🎬 🎤 🎧 🎼 🎵 🎶 🎹 🎻 🎺 🎷 🎸");
		// sports and games
		tmpemoji.add("👾 🎮 🃏 🎴 🀄 🎲 🎯 🏈 🏀 ⚽ ⚾ 🎾 🎱 🏉 🎳 ⛳ 🚵 🚴 🏁 🏇 🏆 🎿 🏂 🏊 🏄 🎣");

		//BLog.e("OUT", "pos: "+tmpemoji.size());
		
		// buildings, locations
		tmpemoji.add("🏠 🏡 🏫 🏢 🏣 🏥 🏦 🏪 🏩 🏨 💒 ⛪ 🏬 🏤 🌇 🌆 🏯 🏰 ⛺ 🏭 🗼 🗾 🗻 🌄 🌅 🌃 🗽 🌉 🎠 🎡 ⛲ 🎢 🚢");
		// transport
		tmpemoji.add("⛵ 🚤 🚣 ⚓ 🚀 ✈ 💺 🚁 🚂 🚊 🚉 🚞 🚆 🚄 🚅 🚈 🚇 🚝 🚋 🚃 🚎 🚌 🚍 🚙 🚘 🚗 🚕 🚖 🚛 🚚 🚨 🚓 🚔 🚒 🚑 🚐 🚲 🚡 🚟 🚠 🚜 💈 🚏 🎫 🚦 🚥 ⚠ 🚧 🔰 ⛽ 🏮 🎰 ♨ 🗿 🎪 🎭 📍 🚩 ");
		
		// toys, tools, technology
		tmpemoji.add("🎍 💝 🎎 🎒 🎓 🎏 🎆 🎇 🎐 🎑 🎃 👻 🎅 🎄 🎁 🎋 🎉 🎊 🎈 🎌 🔮 🎥 📷 📹 📼 💿 📀 💽 💾 💻 📱 ☎ 📞 📟 📠 📡 📺 📻 🔊 🔉 🔈 🔇 🔔 🔕 📢 📣 ⏳ ⌛ ⏰ ⌚ 🔓 🔒 🔏 🔐 🔑 🔎 💡 🔦 🔆 🔅 🔌 🔋 🔍 🛁 🛀 🚿 🚽 🔧 🔩 🔨 🚪 🚬 💣 🔫 🔪 💊 💉 💰 💴 💵 💷 💶 💳 💸 📲 ");
		// books, envelopes and stationary
		tmpemoji.add("📧 📥 📤 ✉ 📩 📨 📯 📫 📪 📬 📭 📮 📦 📝 📄 📃 📑 📊 📈 📉 📜 📋 📅 📆 📇 📁 📂 ✂ 📌 📎 ✒ ✏ 📏 📐 📕 📗 📘 📙 📓 📔 📒 📚 📖 🔖 📛 🔬 🔭 📰");

		

		//BLog.e("OUT", "pos: "+tmpemoji.size());
		
		// flags
        //tmpemoji.add("🇯 🇵 🇰 🇷 🇩 🇪 🇨 🇳 🇺 🇸 🇫 🇷 🇪 🇸 🇮 🇹 🇷 🇺 🇬 🇧");
        tmpemoji.add("🇯🇵 🇰🇷 🇩🇪 🇨🇳 🇺🇸 🇫🇷 🇪🇸 🇮🇹 🇷🇺 🇬🇧");
		// numbers and arrows
		tmpemoji.add("1⃣ 2⃣ 3⃣ 4⃣ 5⃣ 6⃣ 7⃣ 8⃣ 9⃣ 0⃣ 🔟 🔢 #⃣ 🔣 ⬆ ⬇ ⬅ ➡ 🔠 🔡 🔤 ↗ ↖ ↘ ↙ ↔ ↕ 🔄 ◀ ▶ 🔼 🔽 ↩ ↪ ℹ ⏪ ⏩ ⏫ ⏬ ⤵ ⤴");
		// text and labels
		tmpemoji.add("🆗 🔀 🔁 🔂 🆕 🆙 🆒 🆓 🆖 📶 🎦 🈁 🈯 🈳 🈵 🈴 🈲 🉐 🈹 🈺 🈶 🈚 🚻 🚹 🚺 🚼 🚾 🚰 🚮 🅿 ♿ 🚭 🈷 🈸 🈂 Ⓜ 🛂 🛄 🛅 🛃 🉑 ㊙ ㊗ 🆑 🆘 🆔 🚫 🔞 📵 🚯 🚱 🚳 🚷 🚸 ⛔ ✳ ❇ ❎ ✅ ✴ 💟 🆚 📳 📴 🅰 🅱 🆎 🅾 💠 ➿ ♻");
		
		// astrology and zodiac
		tmpemoji.add("♈ ♉ ♊ ♋ ♌ ♍ ♎ ♏ ♐ ♑ ♒ ♓ ⛎ ");
		
		// others symbols and misc
		tmpemoji.add("🔯 🏧 💹 💲 💱 © ® ™ ❌ ‼ ⁉ ❗ ❓ ❕ ❔ ⭕ 🔝 🔚 🔙 🔛 🔜 🔃 🕛 🕧 🕐 🕜 🕑 🕝 🕒 🕞 🕓 🕟 🕔 🕠 🕕 🕖 🕗 🕘 🕙 🕚 🕡 🕢 🕣 🕤 🕥 🕦 ✖ ➕ ➖ ➗ ♠ ♥ ♣ ♦ 💮 💯 ✔ ☑ 🔘 🔗 ➰ 〰 〽 🔱 ◼ ◻ ◾ ◽ ▪ ▫ 🔺 🔲 🔳 ⚫ ⚪ 🔴 🔵 🔻 ⬜ ⬛ 🔶 🔷 🔸 🔹");

        int count=0;
        List<String> addlist = new ArrayList<String>();
		for(String tmpe: tmpemoji) {
			String[] sp=tmpe.split(" ");

			for(int ij=0; ij<sp.length; ij++) {
                emojiiLookup.put(Pattern.compile(Pattern.quote(sp[ij])),0);
                if(count<splitpoints.size() && splitpoints.get(count).equals(sp[ij])) {
                    //BLog.e("MATCHED",""+count);
                    emoji.add(addlist);
                    addlist = new ArrayList<String>();
                    count++;
                }
                addlist.add(sp[ij]);
			}

		}
        emoji.add(addlist);
		//BLog.e("EMO","size: "+emoji.size());
		
		/*
			put(":-)", 0x1F60A);
put(":)", 0x1F60A);
put(":-(", 0x1F61E);
put(":(", 0x1F61E);
put(":-D", 0x1F603);
put(":D", 0x1F603);
put(";-)", 0x1F609);
put(";)", 0x1F609);
put(":-P", 0x1F61C);
put(":P", 0x1F61C);
put(":-p", 0x1F61C);
put(":p", 0x1F61C);
put(":-*", 0x1F618);
put(":*", 0x1F618);
put("<3", 0x2764);
put(":3", 0x2764);
put(">:[", 0x1F621);
put(":'|", 0x1F625);
put(":-[", 0x1F629);
put(":'(", 0x1F62D);
put("=O", 0x1F631);
put("xD", 0x1F601);
put(":')", 0x1F602);
put(":-/", 0x1F612);
put(":/", 0x1F612);
put(":-|", 0x1F614);
put(":|", 0x1F614);
put("*_*", 0x1F60D);
		*/
	    addPattern(emoticons, ":)", R.drawable.emo_im_happy);
	    addPattern(emoticons, ":-)", R.drawable.emo_im_happy);
        addPattern(emoticons, ":>", R.drawable.emo_im_happy);
        addPattern(emoticons, "=)", R.drawable.emo_im_happy);

        emoticonsDefaults.add(":)");
	    
    
	    addPattern(emoticons, "B-)", R.drawable.emo_im_cool);
        addPattern(emoticons, "B)", R.drawable.emo_im_cool);

        emoticonsDefaults.add("B)");

        addPattern(emoticons, ":')", R.drawable.emo_im_crying);
        addPattern(emoticons, ":'-)", R.drawable.emo_im_crying);
        addPattern(emoticons, ":-)", R.drawable.emo_im_crying);
        addPattern(emoticons, ";_;", R.drawable.emo_im_crying);

        emoticonsDefaults.add(":')");

	    addPattern(emoticons, ":-[", R.drawable.emo_im_embarrassed);
        addPattern(emoticons, ":[", R.drawable.emo_im_embarrassed);
        emoticonsDefaults.add(":[");
	    
	    addPattern(emoticons, ":*", R.drawable.emo_im_kissing);
        addPattern(emoticons, ":-*", R.drawable.emo_im_kissing);
        addPattern(emoticons, ":^*", R.drawable.emo_im_kissing);
        emoticonsDefaults.add(":*");
	    
	    // next line
        addPattern(emoticons, ":-3", R.drawable.emo_im_love);
        addPattern(emoticons, "<3", R.drawable.emo_im_love);
        emoticonsDefaults.add("<3");

        addPattern(emoticons, "</3", R.drawable.emo_im_no_love);
        addPattern(emoticons, ":/3", R.drawable.emo_im_no_love);
        emoticonsDefaults.add("</3");

        addPattern(emoticons, "\\o/", R.drawable.emo_im_high_five);
        addPattern(emoticons, "o/\\o", R.drawable.emo_im_high_five);
        addPattern(emoticons, ">5", R.drawable.emo_im_high_five);
        addPattern(emoticons, "^5", R.drawable.emo_im_high_five);
        emoticonsDefaults.add("^5");
	    
	    addPattern(emoticons, ":-D", R.drawable.emo_im_laughing);
        addPattern(emoticons, ":D", R.drawable.emo_im_laughing);
        addPattern(emoticons, ":))", R.drawable.emo_im_laughing);
        addPattern(emoticons, "=D", R.drawable.emo_im_laughing);
        emoticonsDefaults.add(":D");

	    
	    addPattern(emoticons, "O:-)", R.drawable.emo_im_angel);
        addPattern(emoticons, "O:)", R.drawable.emo_im_angel);
        addPattern(emoticons, "0:)", R.drawable.emo_im_angel);
        addPattern(emoticons, "0:-)", R.drawable.emo_im_angel);
        emoticonsDefaults.add("O:)");
	    
	    addPattern(emoticons, ":-X", R.drawable.emo_im_lips_sealed);
        addPattern(emoticons, ":X", R.drawable.emo_im_lips_sealed);
        addPattern(emoticons, ":|", R.drawable.emo_im_lips_sealed);
        addPattern(emoticons, ":-|", R.drawable.emo_im_lips_sealed);
        emoticonsDefaults.add(":|");
	    
	    addPattern(emoticons, ":$", R.drawable.emo_im_money);
        addPattern(emoticons, ":£", R.drawable.emo_im_money);
        addPattern(emoticons, ":-£", R.drawable.emo_im_money);
        addPattern(emoticons, ":€", R.drawable.emo_im_money);
        addPattern(emoticons, ":$", R.drawable.emo_im_money);
        addPattern(emoticons, ":¥", R.drawable.emo_im_money);
        emoticonsDefaults.add(":-£");

	    addPattern(emoticons, ":-!", R.drawable.emo_im_oops);
        addPattern(emoticons, ":!", R.drawable.emo_im_oops);
        addPattern(emoticons, ":-/", R.drawable.emo_im_oops); // puzzled
        emoticonsDefaults.add(":!");


	    addPattern(emoticons, ":-(", R.drawable.emo_im_sad);
	    addPattern(emoticons, ":(", R.drawable.emo_im_sad);
        addPattern(emoticons, ">:(", R.drawable.emo_im_sad);
        addPattern(emoticons, ":<", R.drawable.emo_im_sad);
        emoticonsDefaults.add(":(");

        addPattern(emoticons, ":-\\", R.drawable.emo_im_wtf);
        addPattern(emoticons, "%)", R.drawable.emo_im_wtf);
        addPattern(emoticons, "%-)", R.drawable.emo_im_wtf);
        addPattern(emoticons, "%(", R.drawable.emo_im_wtf);
        addPattern(emoticons, "%-(", R.drawable.emo_im_wtf);
        addPattern(emoticons, "?)", R.drawable.emo_im_wtf);
        addPattern(emoticons, "?-)", R.drawable.emo_im_wtf);
        addPattern(emoticons, "?|", R.drawable.emo_im_wtf);
        emoticonsDefaults.add("?-)");

	    addPattern(emoticons, ":-&", R.drawable.emo_im_suprised);
        addPattern(emoticons, ":&", R.drawable.emo_im_suprised);
        addPattern(emoticons, "o-O", R.drawable.emo_im_suprised);
        addPattern(emoticons, "o_O", R.drawable.emo_im_suprised);
        addPattern(emoticons, "o.O", R.drawable.emo_im_suprised);
        emoticonsDefaults.add("o_O");
	    
	    // next line
	    
	    addPattern(emoticons, ":-P", R.drawable.emo_im_toungue);
        addPattern(emoticons, ":P", R.drawable.emo_im_toungue);
        addPattern(emoticons, ":b", R.drawable.emo_im_toungue);
        addPattern(emoticons, ":-b", R.drawable.emo_im_toungue);
        addPattern(emoticons, ":p", R.drawable.emo_im_toungue);
        addPattern(emoticons, ":-p", R.drawable.emo_im_toungue);

        emoticonsDefaults.add(":P");

	    
	    addPattern(emoticons, ";)", R.drawable.emo_im_winking);
	    addPattern(emoticons, ";-)", R.drawable.emo_im_winking);
        addPattern(emoticons, "*)", R.drawable.emo_im_winking);
        addPattern(emoticons, ";D", R.drawable.emo_im_winking);
        emoticonsDefaults.add(";)");
	    

	    addPattern(emoticons, ":O", R.drawable.emo_im_angry);  // angry
        addPattern(emoticons, ":-O", R.drawable.emo_im_angry);  // angry
        emoticonsDefaults.add(":O");

        addPattern(emoticons, "@>-", R.drawable.emo_im_flower);
        addPattern(emoticons, "@}-", R.drawable.emo_im_flower);
        emoticonsDefaults.add("@}-");

        addPattern(emoticons, "<:)", R.drawable.emo_im_devil);
        addPattern(emoticons, ">:)", R.drawable.emo_im_devil);
        addPattern(emoticons, ">:-)", R.drawable.emo_im_devil);
        addPattern(emoticons, "}:-)", R.drawable.emo_im_devil);
        addPattern(emoticons, "}:)", R.drawable.emo_im_devil);
        addPattern(emoticons, "3:-)", R.drawable.emo_im_devil);
        emoticonsDefaults.add("}:)");

        addPattern(emoticons, "#-)", R.drawable.emo_im_party);
        addPattern(emoticons, "#)", R.drawable.emo_im_party);
        addPattern(emoticons, "::)", R.drawable.emo_im_party);
        emoticonsDefaults.add("::)");

        addPattern(emoticons, "#-|", R.drawable.emo_im_hypnotized);
        addPattern(emoticons, "#|", R.drawable.emo_im_hypnotized);
        addPattern(emoticons, "#O", R.drawable.emo_im_hypnotized);
        addPattern(emoticons, "#/", R.drawable.emo_im_hypnotized);
        emoticonsDefaults.add("#-|");

        addPattern(emoticons, "(-_-)", R.drawable.emo_im_sleeping);
        addPattern(emoticons, ":Zz", R.drawable.emo_im_sleeping);
        addPattern(emoticons, "zzz", R.drawable.emo_im_sleeping);
        addPattern(emoticons, "ZZZ", R.drawable.emo_im_sleeping);

        emoticonsDefaults.add("ZZZ");


	}
    /*
    private static void generateEmojiBitmaps (Context context) {
        AssetManager mngr = context.getAssets();

        for(List<String> inner: emoji) {
            for(String emo: inner) {
                InputStream in = null;
                try {
                    in = mngr.open("emoticons/" + emoji);
                } catch (Exception e){
                    e.printStackTrace();
                }
                Bitmap temp = BitmapFactory.decodeStream(in, null, null);
                if(temp!=null)
                    emojiBitmaps.put(emo,temp);
            }
            //break;
        }

    }

    public static void clearManager() {
        emojiBitmaps=new HashMap<String, Bitmap>();
    }
    */
    public static int getEmoticonsRiconLookup(String emoChars) {
        //BLog.e("EMC",emoChars+": "+emoticonslookup.get(emoChars));
        if(emoticonslookup!=null)
            return emoticonslookup.get(emoChars);
        return 0;
    }
	private static void addPattern(Map<Pattern, Integer> map, String smile,
	        int resource) {
	    map.put(Pattern.compile(Pattern.quote(smile)), resource);
	    emoticonslookup.put(smile, resource);

	}

	private static boolean addSmiles(Context context, Spannable spannable) {
	    boolean hasChanges = false;
	    
	    for (Entry<Pattern, Integer> entry : emoticons.entrySet()) {
	        Matcher matcher = entry.getKey().matcher(spannable);
	        while (matcher.find()) {
	            boolean set = true;
	            for (ImageSpan span : spannable.getSpans(matcher.start(),
	                    matcher.end(), ImageSpan.class))
	                if (spannable.getSpanStart(span) >= matcher.start()
	                        && spannable.getSpanEnd(span) <= matcher.end())
	                    spannable.removeSpan(span);
	                else {
	                    set = false;
	                    break;
	                }
	            if (set) {
	                hasChanges = true;
	                spannable.setSpan(new ImageSpan(context, entry.getValue()),
	                        matcher.start(), matcher.end(),
	                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	            }
	        }
	    }
	    return hasChanges;
	}
    public static boolean reziseEmojii(Context context, Spannable spannable) {
        boolean hasChanges = false;

        for (Entry<Pattern, Integer> entry : emojiiLookup.entrySet()) {
            Matcher matcher = entry.getKey().matcher(spannable);
            while (matcher.find()) {
                boolean set = true;
                for (RelativeSizeSpan span : spannable.getSpans(matcher.start(),
                        matcher.end(), RelativeSizeSpan.class))
                    if (spannable.getSpanStart(span) >= matcher.start()
                            && spannable.getSpanEnd(span) <= matcher.end())
                        spannable.removeSpan(span);
                    else {
                        set = false;
                        break;
                    }
                if (set) {
                    hasChanges = true;
                    spannable.setSpan(new RelativeSizeSpan(2.4F),
                            matcher.start(), matcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return hasChanges;
    }
	private static Spannable getSmiledText(Context context, CharSequence text) {
	    Spannable spannable = spannableFactory.newSpannable(text);
	    addSmiles(context, spannable);
	    return spannable;
	}
	public static void dismissPopup() {
		if(popupWindow!=null)
			popupWindow.dismiss();
	}


    public void addClickListneerOpenEmoji(View parentView, EditText editText, ImageView smilesOpener) {
        useSmsSmile=smilesOpener;
        useSmsSmile.setOnClickListener(getSmilPopupListener(activity, parentView, editText, smilesOpener));
    }

	private static OnClickListener getSmilPopupListener(final Activity activity, final View parentView, final View editTextView, final ImageView smsSmile) {
		return new OnClickListener() {

    		@Override
    		public void onClick(View v) {


                Device.setKeyboard(activity,editTextView,true);
                if (popupWindow.isShowing()) {
                    pager.setOffscreenPageLimit(0);
                    emoticonsCover.setVisibility(LinearLayout.GONE);
                    smsSmile.setImageDrawable(activity.getResources().getDrawable(R.drawable.i_smile));
                    popupWindow.dismiss();
                } else {
                    int useHeight=Device.getKeyboardHeight(activity);
                    if(useHeight==0)
                        useHeight=Functions.dpToPx(230,activity);
                    popupWindow.setHeight(useHeight);
                    popupWindow.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
                    emoticonsCover.setVisibility(LinearLayout.VISIBLE);
                    smsSmile.setImageDrawable(activity.getResources().getDrawable(R.drawable.hardware_keyboard));
                    adapter = new EmoticonsPagerAdapter(activity, ViewManagerText.getEmoji(),VMT.listen);
                    pager.setAdapter(adapter);

                    loadBitmaps.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pager.setOffscreenPageLimit(5);
                        }
                    },400);

                }



    		}
    	};
	}

    private static View bgFaces;
    private static View bgNature;
    private static View bgItems;
    private static View bgEngineering;
    private static View bgMisc;
    private static Drawable borderOff;
    private static Drawable borderOn;

    private static void setButtonOn(View view) {
        bgFaces.setBackground(borderOff);
        bgNature.setBackground(borderOff);
        bgItems.setBackground(borderOff);
        bgEngineering.setBackground(borderOff);
        bgMisc.setBackground(borderOff);
        view.setBackground(borderOn);
    }
    private static void setButtonOn(int position) {
        bgFaces.setBackground(borderOff);
        bgNature.setBackground(borderOff);
        bgItems.setBackground(borderOff);
        bgEngineering.setBackground(borderOff);
        bgMisc.setBackground(borderOff);
        switch(position) {
            case 0: bgFaces.setBackground(borderOn); break;
            case 1: bgNature.setBackground(borderOn); break;
            case 2: bgItems.setBackground(borderOn); break;
            case 3: bgEngineering.setBackground(borderOn); break;
            case 4: bgMisc.setBackground(borderOn); break;
        }

    }
	public static void enablePopUpView(final Activity activity, final EditText message, KeyClickListener listen) {

        VMT.listen=listen;
		//popupView=activity.findViewById(R.id.)
		//emoticonAll = (ImageButton) popUpView.findViewById(R.id.emo_btn_emoticon);
        bgFaces=popUpView.findViewById(R.id.emo_btn_emoj_faces_pod);
        bgNature=popUpView.findViewById(R.id.emo_btn_emoj_nature_pod);
        bgItems=popUpView.findViewById(R.id.emo_btn_emoj_items_pod);
        bgEngineering=popUpView.findViewById(R.id.emo_btn_emoj_engineering_pod);
        bgMisc=popUpView.findViewById(R.id.emo_btn_emoj_misc_pod);

        borderOff=activity.getResources().getDrawable(R.drawable.emo_border_off);
        borderOn=activity.getResources().getDrawable(R.drawable.emo_border_on);

		emoFaces = (EditText) popUpView.findViewById(R.id.emo_btn_emoj_faces);
		emoFaces.setBackground(null);
		emoFaces.setText("😄");
		emoFaces.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				pager.setCurrentItem(0,true);
                v.startAnimation(B.animateAlphaFlash());
                setButtonOn(bgFaces);
			}
		});

		emoNature = (EditText) popUpView.findViewById(R.id.emo_btn_emoj_nature);
		emoNature.setBackground(null);
		emoNature.setText("🐶");
		emoNature.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				pager.setCurrentItem(1,true);
                v.startAnimation(B.animateAlphaFlash());
                setButtonOn(bgNature);
			}
		});

		emoItems = (EditText) popUpView.findViewById(R.id.emo_btn_emoj_items);
		emoItems.setBackground(null);
		emoItems.setText("🍵");
		emoItems.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				pager.setCurrentItem(2,true);
                v.startAnimation(B.animateAlphaFlash());
                setButtonOn(bgItems);
			}
		});
		
		
		emoEngineering = (EditText) popUpView.findViewById(R.id.emo_btn_emoj_engineering);
		emoEngineering.setBackground(null);
		emoEngineering.setText("🏠");
		emoEngineering.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				pager.setCurrentItem(3,true);
                v.startAnimation(B.animateAlphaFlash());
                setButtonOn(bgEngineering);
			}
		});
		
		emoMisc = (EditText) popUpView.findViewById(R.id.emo_btn_emoj_misc);
		emoMisc.setBackground(null);
		emoMisc.setText("🇬🇧");
		emoMisc.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				pager.setCurrentItem(4, true);
                v.startAnimation(B.animateAlphaFlash());
                setButtonOn(bgMisc);
			}
		});

        setButtonOn(bgFaces);
/*
        ImageView emoticcon = (ImageView) popUpView.findViewById(R.id.emo_btn_emoticon);
        emoticcon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                pager.setCurrentItem(5, true);
                v.startAnimation(B.animateAlphaFlash());
            }
        });
		*/
		pager = (ViewPager) popUpView.findViewById(R.id.emoticons_pager);
        pager.setOffscreenPageLimit(0);
		//ArrayList<String> paths = new ArrayList<String>();
    pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            setButtonOn(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    });



		// Creating a pop window for emoticons keyboard
		popupWindow = new PopupWindow(popUpView, LayoutParams.MATCH_PARENT,
				(int) Device.getKeyboardHeight(activity), false);

        ImageView backSpace = (ImageView) popUpView.findViewById(R.id.back);
		backSpace.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
				message.dispatchKeyEvent(event);	
			}
		});
        ImageView spaceKey = (ImageView) popUpView.findViewById(R.id.space);
        spaceKey.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_SPACE, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                message.dispatchKeyEvent(event);
            }
        });
        ImageView newline = (ImageView) popUpView.findViewById(R.id.newline);
        newline.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_ENTER, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                message.dispatchKeyEvent(event);
            }
        });
/*
		ImageView keyboard = (ImageView) popUpView.findViewById(R.id.eicon_keyboard);
		keyboard.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
    			if(Device.isKeyboardVisible()) {
        				emoticonsCover.setVisibility(LinearLayout.GONE);
    			} else {
    					emoticonsCover.setVisibility(LinearLayout.GONE);
    					popupWindow.dismiss();
    			}
                if(useSmsSmile!=null)
                    useSmsSmile.setImageDrawable(activity.getResources().getDrawable(R.drawable.hardware_keyboard));
			}
		});
*/
		popupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				emoticonsCover.setVisibility(LinearLayout.GONE);
			}
		});
	}
	

	OnAttachStateChangeListener att = new OnAttachStateChangeListener() {
		@Override
		public void onViewAttachedToWindow(View v) {
			//BLog.e("ATW", "attached...");
		}
		@Override
		public void onViewDetachedFromWindow(View v) {

		}
	};
    /*
    public void setEditText(String text) {
    	editField.removeOnAttachStateChangeListener(att);
    	editField.removeTextChangedListener(tw);
    	
    	Spannable str = new SpannableString(text);
    	addSmiles(activity,str);
		editField.setText(str);
		
    	editField.addTextChangedListener(tw);
    	editField.addOnAttachStateChangeListener(att);

    }
	*/
    private int lenStart=0;
    private int lenEnd=0;
	TextWatcher tw = new TextWatcher() {
		

		//private Spannable before;
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        	
        	
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        	
        	
        	lenStart=editField.getSelectionStart();
        	lenEnd=editField.length(); //editField.getSelectionEnd();
        	
        	
        }


        
        @Override
        public void afterTextChanged(Editable s) {
        	//Device.
        	//int imeopt = editField.getImeOptions();
        	//CharSequence label =editField.getImeActionLabel();
        	//int id=editField.getImeActionId();
            preTextChange();
            doAfterTextChange(s.toString());
            apresTextChange();
        	//editField.addOnLayoutChangeListener(layy);
        	//editField.setImeOptions(imeopt);
        	//editField.setImeActionLabel(label, id);
        	//BLog.e("LABEL", label.toString());
        	/*
        	            //BLog.e("ATW", "detracted---");
            if(VMT.refreshable!=null) {
                BLog.e("changeText", "now");
                VMT.refreshable.refreshData();
            }
        	int len=s.length();
        	if(len!=lastLength) {
            	if(len>lastLength+1) {
            		lastLength=len;
            		editField.setText(getSmiledText(activity, s));
            		editField.setSelection(lastLength);
            	}
        	
        		
        	}
        	*/
        }
    };
	
	private void applyEmoticonsToEditText() {
		Spannable str = new SpannableString(editField.getText().toString());
		addSmiles(activity,str);
		editField.setText(str);
		editField.refreshDrawableState();
		editField.addTextChangedListener(tw);
		editField.addOnAttachStateChangeListener(att);
	}
    public void preTextChange() {
        editField.removeOnAttachStateChangeListener(att);
        editField.removeTextChangedListener(tw);
    }
    public void apresTextChange() {
        editField.addTextChangedListener(tw);
        editField.addOnAttachStateChangeListener(att);


        Bgo.tryRefreshDataCurrentFragment();
    }
    public void doAfterTextChange(String s) {
        //BLog.e("CH",s.toString()+"--"+editField.getText().toString());

        //editField.removeOnLayoutChangeListener(layy);
        String useString=editField.getText().toString();//s.toString();

        int start3 = lenStart-2<0?0:lenStart-2; //useString.length()-3<0?0:useString.length()-3;
        int start2= lenStart-1<0?0:lenStart-1; //useString.length()-2<0?0:useString.length()-2;

        int useLength = lenStart;
        useLength++;
        if(useLength>=useString.length())
            useLength=useString.length();


        String last3 = "";//useString.substring(start3,useLength); //useString.substring(start3,useString.length());
        String last2 = ""; ///useString.substring(start2,useLength); //useString.substring(start2,useString.length());
        if(useString.length()>1) {
            try {
                last3 = useString.substring(start3, useLength); //useString.substring(start3,useString.length());
                last2 = useString.substring(start2, useLength); //useString.substring(start2,useString.length());
            } catch(Exception e ){}
        }

        /*
        if(emoticonslookup.get(s)!=null) {
            Spannable str = new SpannableString(s.toString());

            addSmiles(activity,str);
            editField.setText(str);


            int len=editField.length();
            lenStart = lenStart + (len-lenEnd);
            editField.setSelection(lenStart);
        } else
        */
        if(emoticonslookup.get(last2)!=null || emoticonslookup.get(last3)!=null) {
            //editField.removeOnAttachStateChangeListener(listener);
            Spannable str = new SpannableString(s.toString());

            addSmiles(activity,str);
            editField.setText(str);


            int len=editField.length();
            lenStart = lenStart + (len-lenEnd);
            editField.setSelection(lenStart);

        }

    }
}
