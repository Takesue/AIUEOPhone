package jp.takes.apps.aiueophone;

import java.util.ArrayList;
import java.util.HashMap;

import jp.takes.apps.aiueophone.base.BaseActivity;
import jp.takes.apps.aiueophone.data.AdressData;
import jp.takes.apps.aiueophone.data.CommonData;
import jp.takes.apps.aiueophone.util.CaseConverterUtil;
import jp.takes.apps.aiueophone.util.Messages;
import jp.takes.apps.aiueophone.util.ToastUtil;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;

/**
 * 電話帳データの一覧を表示する画面
 * （４件づつ表示する）
 * @author ict
 *
 */
public class AdressListActivity extends BaseActivity {
	
	/* アドレス情報を格納するクラスの配列 */
	public	AdressData[] adressList = null;
	
	/* 検索のキーとなる文字列 */
	public	String selectCase = null;

	/* アドレス情報格納クラス配列の現在表示対象の位置 */
	public	Integer position = 0;
	
	/* 表示するアドレスの数 */
	private	Integer displayBarNum = 4;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.startLog(new Throwable());		// メソッド開始ログ
		super.onCreate(savedInstanceState);
		setContentView(R.layout.adress_list);

		Intent i = this.getIntent();
		selectCase = i.getStringExtra(CommonData.INTENT_NAME_DAN);

		this.showAdressList();
		this.endLog(new Throwable());		// メソッド終了ログ
	}

	@Override
	protected void onStop() {
		this.startLog(new Throwable());		// メソッド開始ログ
		super.onStop();
		this.endLog(new Throwable());		// メソッド終了ログ
	}
	
	/**
	 * 電話相手の名前が押下された場合
	 * 電話相手の詳細画面へ遷移
	 * @param view
	 */
	public void pressedNameButton(View view) {
		this.startLog(new Throwable());		// メソッド開始ログ

		// 押されたボタンが上から何番目かを格納する
		Integer pos = -1;

		Button btn = (Button)view;
		Integer id = btn.getId();
		
		if (id == R.id.Button01) {
			pos = 0;
		}
		else if (id == R.id.Button02) {
			pos = 1;
		}
		else if (id == R.id.Button03) {
			pos = 2;
		}
		else if (id == R.id.Button04) {
			pos = 3;
		}
		
		// 電話相手の詳細画面へ遷移
		Intent i = new Intent(this, AdressDetailActivity.class);
		i.putExtra(CommonData.INTENT_NAME_NAME, adressList[position + pos].displayName);
		i.putExtra(CommonData.INTENT_NAME_KANA, adressList[position + pos].kanaNameHan);
		i.putExtra(CommonData.INTENT_NAME_NUMBER, adressList[position + pos].phoneNum);
		this.startActivityForResult(i, 0);

		this.endLog(new Throwable());		// メソッド終了ログ
	}
	
	/** 
	 * startActivityForResultで起動したアクティビティから復帰した場合に呼ばれるメソッド
	 * @author take
	 * */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == RESULT_OK) {
			switch(requestCode) {
			case 0:	// 電話相手の詳細画面から復帰
				break;
			case 1:	// 画面から復帰
				break;
			case 2:	// 画面から復帰
				break;
			}
		}

	}


	/**
	 * 連絡先(電話帳)データを取得します。
	 * コンテンツプロバイダを使用して、電話番号を保持している連絡先を抽出して配列データとして取得する。
	 * @return 名前、フリガナ、電話番号情報を保持したオブジェクトの配列（フリガナでソート済み）
	 *
	 */
	private AdressData[] getAddressData() {
		this.startLog(new Throwable());		// メソッド開始ログ
		
        //アドレス情報の取得
        ContentResolver cr = this.getContentResolver();

        /* MIMETYPE=vnd.android.cursor.item/phone_v2の場合、電話番号レコードが取得できる。
         * 電話番号レコードの場合、data1 には電話番号が格納されている。
         */
		Cursor phoneNumberCursor = cr.query(ContactsContract.Data.CONTENT_URI,
				null,
				ContactsContract.Data.MIMETYPE + " = ?",
				new String[] { "vnd.android.cursor.item/phone_v2" },
				null);

		// ハッシュ変数にアドレスを格納(id, phoneNumber)
		HashMap<String, String> mailHash = new HashMap<String, String>();
		while (phoneNumberCursor.moveToNext()) {
			mailHash.put(phoneNumberCursor.getString(phoneNumberCursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)), // ID
					phoneNumberCursor.getString(phoneNumberCursor.getColumnIndex(ContactsContract.Data.DATA1))); // 電話番号
		}
		phoneNumberCursor.close();
		
		if (mailHash.size() == 0) {
			ToastUtil.showLong(this, this.getMessage(Messages.W1002));
			this.log(this.getMessage(Messages.W1002));

			// 表示できないので戻る
			this.finish();
			return null;
		}

		// ソート文字を格納（連絡先一覧を "ふりがな" 順でソート）
		String order_str = " CASE"
				+ " WHEN IFNULL("
				+ ContactsContract.Data.DATA9
				+ ", '') = ''"						// Data.DATA9がNULLの場合は空文字を代入
				+ " THEN 1 ELSE 0"					// Data.DATA9が空文字のレコードを最後にする
				+ " END, " + ContactsContract.Data.DATA9 + " ," + " CASE"
				+ " WHEN IFNULL(" + ContactsContract.Data.DATA7 + ", '') = ''"
				+ " THEN 1 ELSE 0" + " END, " + ContactsContract.Data.DATA7;

		// DATA表から連絡先名を全て取得
		/*
		 * MIMETYPE=vnd.android.cursor.item/nameの場合、連絡先名レコードが取得できる。
		 * 連絡先名レコードの場合、data1 には表示名（姓名）、data2 には表示名（名）、data3 には表示名（姓）、data7
		 * にはふりがな（名）、data9 にはふりがな（姓）が格納されている。
		 */
		Cursor dataNamecursor = cr.query(ContactsContract.Data.CONTENT_URI,
				null,
				ContactsContract.Data.MIMETYPE + " = ?",
				new String[] { "vnd.android.cursor.item/name" },
				order_str);

		// メールアドレスが存在する連絡先だけを名前格納用リストに格納
		ArrayList<String> listNames = new ArrayList<String>(); // 名前格納用リスト
		ArrayList<String> listKanaNames = new ArrayList<String>(); // ふりがな格納用リスト
		ArrayList<String> listPhoneNums = new ArrayList<String>(); // 電話番号格納用リスト
		
		while (dataNamecursor.moveToNext()) {
			String id = dataNamecursor.getString(dataNamecursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));

			/* CONTACT_IDが一致した場合、名前を格納する*/
			if (mailHash.containsKey(id)) {
				listNames.add(dataNamecursor.getString(dataNamecursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)));
				
				// data9 にはふりがな（姓）　data7 にはふりがな（名）を連結してふりがなとして格納
				String kanaTemp = "";
				String tmp = dataNamecursor.getString(dataNamecursor.getColumnIndex(ContactsContract.Data.DATA9));
				if (tmp != null) {
					kanaTemp = tmp;
				}

				tmp = dataNamecursor.getString(dataNamecursor.getColumnIndex(ContactsContract.Data.DATA7));
				if (tmp != null) {
					kanaTemp = kanaTemp + tmp;
				}
				
				// ふりがなを全角カタカナに変更してから格納する。
				listKanaNames.add(kanaTemp);

				// 電話番号を格納する
				listPhoneNums.add(mailHash.get(id));
			}
		}
		dataNamecursor.close();
		
		CaseConverterUtil conUtil = new CaseConverterUtil();
		Integer num = listKanaNames.size();
		AdressData[] adressData = new AdressData[num];
		for (Integer i = 0; i < num; i++) {
			adressData[i] = new AdressData();
			adressData[i].displayName = listNames.get(i);
			adressData[i].kanaNameZen = conUtil.changeKanaCode(CaseConverterUtil.MODE_ZEN, listKanaNames.get(i));
			adressData[i].kanaNameHan = conUtil.changeKanaCode(CaseConverterUtil.MODE_HAN, listKanaNames.get(i));
			adressData[i].phoneNum = listPhoneNums.get(i);
		}
		
		// adressListをかな順にソートする。
		java.util.Arrays.sort(adressData);
		
		this.endLog(new Throwable());		// メソッド終了ログ

		return adressData;
	}
	
	
	/**
	 * 電話帳からデータを取得しアドレス情報一覧を表示する。
	 */
	private void showAdressList() {
		
		// 電話帳からデータを取得
		adressList = this.getAddressData();
		if (adressList == null) {
			// データ取得できない場合、画面表示不可のため処理終了
			return;
		}

		// 検索キーを半角カナに変換
		this.selectCase = new CaseConverterUtil().changeKanaCode(CaseConverterUtil.MODE_ZEN, this.selectCase);
		this.log("selectCase = " + this.selectCase, new Throwable());
		
		this.position = this.matchCase(this.adressList, this.selectCase);
		this.log("position = " + this.position, new Throwable());

		this.showList(0);
	}
	
	/**
	 * 第2引数で指定した文字列が第一引数の配列のカナ文字列と比較し
	 * 第2引数文字列が配列に挿入される場合の位置をを返す。
	 * @param strList ソート済み前提
	 * @return　position　AdressData[]の該当位置を返却する。
	 */
	private Integer matchCase(AdressData[] strList, String matchCase) {

		Integer listNum = strList.length;
		Integer position = 0;

		if (matchCase.compareTo("ア") >= 0) {
			// 引数の配列分ループ
			for (position = 0; position < listNum; position++) {
				if (strList[position].kanaNameZen.compareTo(matchCase) >= 0) {
					break;
				}
			}
		}
		return position;
	}
	
	
	/**
	 * アドレスリストの情報を表示する。
	 * @param cnt アドレスリストの配列の現在位置からのオフセット値を設定
	 */
	private void showList(int cnt) {
		// 初期化
		((Button)this.findViewById(R.id.Button05)).setEnabled(true);
		((Button)this.findViewById(R.id.Button06)).setEnabled(true);
		
		// ポジションをカウントアップする。
		this.position = this.position + cnt;
		
		if (this.position + this.displayBarNum >= this.adressList.length) {
			// ポジションが最後尾に来たらポジションにを設定
			this.position = this.adressList.length - this.displayBarNum;
			// 「次へ」ボタンを無効にする
			((Button)this.findViewById(R.id.Button06)).setEnabled(false);		// Viewを無効
		}

		if (this.position <= 0) {
			// ポジションが先頭に来たらポジションに0を設定
			this.position = 0;
			// 「前へ」ボタンを無効にする
			((Button)this.findViewById(R.id.Button05)).setEnabled(false);		// Viewを無効
		}
		
		Button[] buttonList = new Button[4];
		buttonList[0] = (Button)this.findViewById(R.id.Button01);
		buttonList[1] = (Button)this.findViewById(R.id.Button02);
		buttonList[2] = (Button)this.findViewById(R.id.Button03);
		buttonList[3] = (Button)this.findViewById(R.id.Button04);

		for(int i = 0; i < this.displayBarNum; i++) {
			if ((i + this.position) < this.adressList.length) {
				buttonList[i].setText(this.adressList[this.position + i].displayName + "\n" + this.adressList[this.position + i].kanaNameHan);
			}
			else {
				// 表示する電話帳の件数が４件よりも小さい場合、空のアイテムは無効にする
				buttonList[i].setText("");
				buttonList[i].setEnabled(false);
			}
		}
	}
	
	/**
	 * 「前へ」ボタン押下時に呼ばれる。
	 * @param view
	 */
	public void prevList(View view) {
		this.showList(-this.displayBarNum);
	}

	/**
	 * 「次へ」ボタン押下時に呼ばれる。
	 * @param view
	 */
	public void nextList(View view) {
		this.showList(this.displayBarNum);
	}
	
	/**
	 * 表示文字列のサイズを変更する。
	 */
	public void changeCaseSize() {
		this.startLog(new Throwable());
		
		// 文字サイズ(SP)を各部品に設定する
		((Button)this.findViewById(R.id.Button01)).setTextSize(this.getCaseSizeSP());
		((Button)this.findViewById(R.id.Button02)).setTextSize(this.getCaseSizeSP());
		((Button)this.findViewById(R.id.Button03)).setTextSize(this.getCaseSizeSP());
		((Button)this.findViewById(R.id.Button04)).setTextSize(this.getCaseSizeSP());
		((Button)this.findViewById(R.id.Button05)).setTextSize(this.getCaseSizeSP());
		((Button)this.findViewById(R.id.Button06)).setTextSize(this.getCaseSizeSP());

		this.endLog(new Throwable());
	}

}
