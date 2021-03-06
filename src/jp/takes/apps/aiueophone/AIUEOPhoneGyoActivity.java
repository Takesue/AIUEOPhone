package jp.takes.apps.aiueophone;

import java.util.ArrayList;
import jp.takes.apps.aiueophone.base.BaseActivity;
import jp.takes.apps.aiueophone.data.CommonData;
import jp.takes.apps.aiueophone.util.AlertDialogUtil;
import jp.takes.apps.aiueophone.util.CaseConverterUtil;
import jp.takes.apps.aiueophone.util.Messages;
import jp.takes.apps.aiueophone.util.ToastUtil;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 行選択する画面
 * ア行〜ワ行から電話相手の頭文字で選択する
 * @author ict
 *
 */
public class AIUEOPhoneGyoActivity extends BaseActivity implements OnClickListener {
	
	/* 音声認識の対象文字列 */
	public String[] strNameList = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// アプリの初期処理を実施(ルートの画面でのみ呼び出す)
		this.init();

		// メッセージクラスのテスト用に使用
		this.log(this.getMessage(Messages.I0001, this.getString(R.string.app_name)));

		this.setContentView(R.layout.gyo_case);
	}

	@Override
	protected void onStop() {
		this.startLog(new Throwable());		// メソッド開始ログ
		super.onStop();
		this.endLog(new Throwable());		// メソッド終了ログ
	}
	
	
	/**
	 * あ-わ行のボタンが押下された場合
	 * 各行の文字選択画面へ遷移
	 * @param view
	 */
	public void pressedGyouButton(View view) {
		String dispName = ((TextView)view).getText().toString();
		// 段ボタン一覧表示画面へ遷移
		Intent i = new Intent(this, AIUEOPhoneDanActivity.class);
		i.putExtra(CommonData.INTENT_NAME_GYO, dispName);
		this.startActivityForResult(i, 0);
	}
	
	/**
	 * 「表示」ボタンをクリックした場合
	 * @param view
	 */
	public void pressedAllDispButton(View view) {
		this.startLog(new Throwable());		// メソッド開始ログ
		// 検索キーなしでアドレス一覧表示画面へ遷移する
		this.callAdressListActivity("");
		this.endLog(new Throwable());		// メソッド終了ログ
	}

	/**
	 * 「設定」ボタンをクリックされた場合
	 * @param view
	 */
	public void pressedPreferenceButton(View view) {
		// 設定画面へ遷移
		Intent i = new Intent(this, MainPreferenceActivity.class);
		this.startActivityForResult(i, 1);
	}

	/**
	 * 音声検索ボタンをクリックした場合
	 * @param view
	 */
	public void pressedVoiceButton(View view) {
		try {
			// 音声認識の準備
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, this.getString(R.string.anaunce_voice));
			// インテント発行
			this.startActivityForResult(intent, 1);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, this.getString(R.string.NotExistVoiceApl), Toast.LENGTH_LONG).show();
			this.log(this.getMessage(Messages.W1001));
		}
	}
	
	
	/**
	 * 音声認識アプリで取得した複数の文字列から検索キーを選択した場合に呼ばれる
	 * @param name
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		String input = "";
		input = strNameList[which];
		this.callAdressListActivity(input);
		
	}

	/**
	 * 引数で渡された検索キー文字列を引数としてアドレス一覧表示画面を呼び出す
	 * @param name
	 */
	private void callAdressListActivity(String name) {
		// アドレス一覧表示画面へ遷移
		Intent i = new Intent(this, AdressListActivity.class);
		// 先頭を表示するため、""を設定する
		i.putExtra(CommonData.INTENT_NAME_DAN, name);
		this.startActivityForResult(i, 2);
	}

	
	/** 
	 * startActivityForResultで起動したアクティビティから復帰した場合に呼ばれるメソッド
	 * @author take
	 **/
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == RESULT_OK) {
			switch(requestCode) {
			case 0:	// 段ボタン一覧表示画面画面から復帰
				break;
			case 1:	// 音声認識画面から復帰
				ArrayList<String> candidates = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				this.log("音声検索結果の数 = " + candidates.size());

				Integer num = candidates.size();
				
				ArrayList<String> listNames = new ArrayList<String>();
				
				this.strNameList = new String[num];
				for (Integer i = 0; i< num ; i++) {
					CaseConverterUtil util = new CaseConverterUtil();
					String moji = candidates.get(i);
					if (util.judgeWhetherKanaString(moji)) {
						// 全てヒラガナの文字列のみが対象とする。
						listNames.add(candidates.get(i));
					}
					
					this.log("音声検索結果の候補 = " + candidates.get(i));
				}
				this.strNameList = (String[])listNames.toArray(new String[0]);
				
				if(strNameList.length > 0) {
					// 候補が取得できているので
					// 候補一覧をダイアログで表示する
					AlertDialogUtil.showAlert(this, this.getString(R.string.Candidatelist), this.strNameList);
				}
				else {
					ToastUtil.showLong(this,"キーワードが取得できませんでした。");
				}
				break;
			case 2:	// アドレス一覧表示画面
				break;
			}
		}
	}

	@Override
	public void changeCaseSize() {
		// 文字サイズ(SP)を各部品に設定する
		((Button)this.findViewById(R.id.button_0101)).setTextSize(this.getCaseSizeSP());
		((Button)this.findViewById(R.id.button_0102)).setTextSize(this.getCaseSizeSP());
		((Button)this.findViewById(R.id.button_0103)).setTextSize(this.getCaseSizeSP());
		((Button)this.findViewById(R.id.button_0201)).setTextSize(this.getCaseSizeSP());
		((Button)this.findViewById(R.id.button_0202)).setTextSize(this.getCaseSizeSP());
		((Button)this.findViewById(R.id.button_0203)).setTextSize(this.getCaseSizeSP());
		((Button)this.findViewById(R.id.button_0301)).setTextSize(this.getCaseSizeSP());
		((Button)this.findViewById(R.id.button_0302)).setTextSize(this.getCaseSizeSP());
		((Button)this.findViewById(R.id.button_0303)).setTextSize(this.getCaseSizeSP());
		((Button)this.findViewById(R.id.button_0401)).setTextSize(this.getCaseSizeSP());
		((Button)this.findViewById(R.id.button_0402)).setTextSize(this.getCaseSizeSP());
		((Button)this.findViewById(R.id.button_0403)).setTextSize(this.getCaseSizeSP());
	}
}