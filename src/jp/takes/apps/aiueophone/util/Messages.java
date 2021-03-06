package jp.takes.apps.aiueophone.util;

import android.app.Activity;


/**
 * メッセージIDとメッセージ本文との連携のために使用する。
 * @author take
 */
public class Messages {
	
	//// 自動生成部分　ここから
    /* メッセージID一覧 */
    /* messages.xmlとの連携 */
    public static final String I0001 = "I0001";
    public static final String I0002 = "I0002";
    public static final String I0003 = "I0003";
    public static final String I0004 = "I0004";
    public static final String W1001 = "W1001";
    public static final String W1002 = "W1002";
    public static final String W1003 = "W1003";
    public static final String E2001 = "E2001";
    public static final String E2002 = "E2002";
	//// 自動生成部分　ここまで

	/**
	 * メッセージIDに対応するメッセージを取得する
	 * @param activity　
	 * @param id　メッセージID
	 * @param args　可変文字列(%s)の配列
	 * @return　該当のメッセージが存在しない場合、空文字列を返却する
	 */
	public static String getMessage(Activity activity, String id, String[] args) {
		String message = "";
		
		// リソースIDを取得
		int resId = activity.getResources().getIdentifier(id, "string",
				activity.getPackageName());

		// XMLリソースからメッセージフォーマットを取得
		try {
			message = activity.getString(resId);
		} catch (Exception e) {
			LogUtil.d("MessageManager.getMessage()", String.format("指定したメッセージIDは存在しません。 Message id = %s", id));
			e.printStackTrace();
			return "";
		}
		
		// フォーマットの可変文字列数を取得
		Integer argNum = Messages.analyzeMessageFormat(message);
//		LogUtil.d("MessageManager.getMessage()", "フォーマット = " + strFormat);
		
		// 
		switch(argNum) {
		case 0:
			// メッセージのまま返却
			break;
		case 1:
			if ((args != null) && (args.length == 1)) {
				message = String.format(message, args[0]);
			}
			break;
		case 2:
			if ((args != null) && (args.length == 2)) {
				message = String.format(message, args[0], args[1]);
			}
			break;
		case 3:
			if ((args != null) && (args.length == 3)) {
				message = String.format(message, args[0], args[1], args[2]);
			}
			break;
		}
		return message;
	}
	
	
	/**
	 * メッセージのフォーマット形式を解析する
	 * @param message
	 */
	private static Integer analyzeMessageFormat(String message) {
		
		// $sの数を数える
		String[] item = {"%1$s", "%2$s", "%3$s"};
		
		Integer count = 0;
		for (int i = 0; i < item.length; i++) {
			if (message.indexOf(item[i]) >= 0) {
				count++;
			}
		}
		
		return count;
	}
	
}
