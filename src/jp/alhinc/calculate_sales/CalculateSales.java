package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

    // 支店定義ファイル名
    private static final String FILE_NAME_BRANCH_LST = "branch.lst";

    // 支店別集計ファイル名
    private static final String FILE_NAME_BRANCH_OUT = "branch.out";

    // エラーメッセージ
    private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
    private static final String FILE_NAME_NOT_SEQUENTIAL = "売上ファイル名が連番になっていません";
    private static final String EXCEED_MAX_AMOUNT = "合計金額が10桁を超えました";
    private static final String INVALID_STORE_CODE = "の支店コードが不正です";
    private static final String INVALID_FILE_FORMAT = "のフォーマットが不正です";
    private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
    private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";
    private static final String SALES_AMOUNT_PATTERN = "^[0-9]+$";

    /**
     * メインメソッド
     *
     * @param コマンドライン引数
     */
    public static void main(String[] args) {

    	// コマンドライン引数が渡されているか判定
    	if (args.length != 1) {
    		//コマンドライン引数が1つ設定されていなかった場合は、
    	    //エラーメッセージをコンソールに表⽰します。
    		System.out.println(UNKNOWN_ERROR);
    		return;
    	}

        // 支店コードと支店名を保持するMap
        Map<String, String> branchNames = new HashMap<>();
        // 支店コードと売上金額を保持するMap
        Map<String, Long> branchSales = new HashMap<>();

        // 支店定義ファイル読み込み処理
        if (!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
            return;
        }

        // ※ここから集計処理を作成してください。(処理内容2-1、2-2)

        // 指定されたフォルダ（args[0]）内のファイル一覧を取得する
        File[] files = new File(args[0]).listFiles();

        // 売上ファイルを保持するためのリスト
        List<File> rcdFiles = new ArrayList<>();

        // 配列を回して、条件に合うファイル（数字8桁.rcd）だけをListに入れる
        for (int i = 0; i < files.length; i++) {
        	//対象がファイルであり、「数字8桁.rcd」なのか判定します。
            if (files[i].isFile() && files[i].getName().matches("^[0-9]{8}\\.rcd$")) {
                rcdFiles.add(files[i]);
            }
        }

        BufferedReader br = null;

        //昇順に並べ替え
        Collections.sort(rcdFiles);

        //⽐較回数は売上ファイルの数よりも1回少ないため、
        //繰り返し回数は売上ファイルのリストの数よりも1つ⼩さい数です。
        for(int i = 0; i < rcdFiles.size() -1; i++) {

            //リストから現在のファイル名と次のファイル名を取得
        	int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
        	int latter = Integer.parseInt(rcdFiles.get(i+1).getName().substring(0, 8));

             //⽐較する2つのファイル名の先頭から数字の8⽂字を切り出し、int型に変換します。
        	if((latter - former) != 1) {
        		//2つのファイル名の数字を⽐較して、差が1ではなかったら、
        		//エラーメッセージをコンソールに表⽰します。
        		System.out.println(FILE_NAME_NOT_SEQUENTIAL);
        		return;
        	}
        }
        // リストに格納された売上ファイルの数だけ集計を繰り返す
        for (int i = 0; i < rcdFiles.size(); i++) {
            try {
            	File rcdFile = rcdFiles.get(i);
                // ファイルを読み込む準備）
                br = new BufferedReader(new FileReader(rcdFile));

                // ファイルの中身を溜めるためのリストを作成
                List<String> fileContents = new ArrayList<>();
                String line;

                // while文で最後まで読み込み、リストに入れる
                while ((line = br.readLine()) != null) {
                    fileContents.add(line);
                }

                if (fileContents.size() != 2) {
                	//⽀店情報を保持しているMapに売上ファイルの⽀店コードが存在しなかった場合は、
                    //エラーメッセージをコンソールに表⽰します。
                	System.out.println(rcdFile.getName() + INVALID_FILE_FORMAT);
                	return;
                }

                // リストから1行目（支店コード）と2行目（売上金額）を取り出す
                String storeCode = fileContents.get(0);
                String salesAmount = fileContents.get(1);

                if (!branchNames.containsKey(storeCode)) {
                	//⽀店情報を保持しているMapに売上ファイルの⽀店コードが存在しなかった場合は、
                    //エラーメッセージをコンソールに表⽰します。
                	System.out.println(rcdFile.getName() + INVALID_STORE_CODE);
                	return;
                }

                //売上ファイルの売上金額が数字であるか判定
				if(!salesAmount.matches(SALES_AMOUNT_PATTERN)) {
					//売上⾦額が数字ではなかった場合は、
				    //エラーメッセージをコンソールに表⽰します。
					System.out.println(UNKNOWN_ERROR);
					return;
				}

                // 文字列だった売上額を、計算ができる数値（long型）に変換する
                long amount = Long.parseLong(salesAmount);

                long total = branchSales.get(storeCode) + amount;

                if(total >= 10000000000L) {
                	//売上⾦額が11桁以上の場合、エラーメッセージをコンソールに表⽰します。
                	System.out.println(EXCEED_MAX_AMOUNT);
                	return;
                }

                // Mapから「今の合計金額」を取り出し、今回の売上を足して、Mapに保存し直す
                branchSales.put(storeCode, total);

            } catch (IOException e) {
                System.out.println(UNKNOWN_ERROR);
                return;
            } finally {
                // ファイルを開いている場合は確実に閉じる
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        System.out.println(UNKNOWN_ERROR);
                        return;
                    }
                }
            }
        }

        // 支店別集計ファイル書き込み処理
        if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
            return;
        }
    }

    /**
     * 支店定義ファイル読み込み処理
     *
     * @param フォルダパス
     * @param ファイル名
     * @param 支店コードと支店名を保持するMap
     * @param 支店コードと売上金額を保持するMap
     * @return 読み込み可否
     */
    private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
        BufferedReader br = null;

        try {
            File file = new File(path, fileName);

            if (!file.exists()) {
            	 //⽀店定義ファイルが存在しない場合、コンソールにエラーメッセージを表⽰します。
                System.out.println(FILE_NOT_EXIST);
                return false;
            }

            FileReader fr = new FileReader(file);
            br = new BufferedReader(fr);

            String line;
            // 一行ずつ読み込む
            while ((line = br.readLine()) != null) {
                // ※ここの読み込み処理を変更してください。(処理内容1-2)

                // 文字列を「,」で分割後、配列に格納
                String[] items = line.split(",");

                //⽀店定義ファイルの仕様が満たされていない場合、 エラーメッセージをコンソールに表⽰します。
                if ((items.length != 2) || (!items[0].matches("^[0-9]{3}$"))) {
                    System.out.println(FILE_INVALID_FORMAT);
                    return false;
                }

                // 支店コード、支店名をそれぞれMapに格納する
                branchNames.put(items[0], items[1]);

                // 支店コード、売上額をそれぞれMapに格納する
                branchSales.put(items[0], 0L);
            }

        } catch (IOException e) {
            System.out.println(UNKNOWN_ERROR);
            return false;
        } finally {
            // ファイルを開いている場合
            if (br != null) {
                try {
                    // ファイルを閉じる
                    br.close();
                } catch (IOException e) {
                    System.out.println(UNKNOWN_ERROR);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 支店別集計ファイル書き込み処理
     *
     * @param フォルダパス
     * @param ファイル名
     * @param 支店コードと支店名を保持するMap
     * @param 支店コードと売上金額を保持するMap
     * @return 書き込み可否
     */
    private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
        // ※ここに書き込み処理を作成してください。(処理内容3-1)

        BufferedWriter bw = null;

        try {
            File file = new File(path, fileName);
            bw = new BufferedWriter(new FileWriter(file));

            // Mapにある支店コードの数だけ繰り返す
            for (String code : branchNames.keySet()) {
                // 書き出す文字列を作る（コード + "," + 名前 + "," + 金額
                // ファイルに書き出す
                bw.write(code + "," + branchNames.get(code) + "," + branchSales.get(code));

                // 改行を入れる
                bw.newLine();
            }

        } catch (IOException e) {
            System.out.println(UNKNOWN_ERROR);
            return false;
        } finally {
            // ファイルを開いている場合
            if (bw != null) {
                try {
                    // ファイルを閉じる
                    bw.close();
                } catch (IOException e) {
                    System.out.println(UNKNOWN_ERROR);
                    return false;
                }
            }
        }
        return true;
    }
}