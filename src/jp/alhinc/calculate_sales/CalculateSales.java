package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
    private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
    private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

    /**
     * メインメソッド
     *
     * @param コマンドライン引数
     */
    public static void main(String[] args) {
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
            if (files[i].getName().matches("^[0-9]{8}\\.rcd$")) {
                rcdFiles.add(files[i]);
            }
        }

        BufferedReader br = null;
        // リストに格納された売上ファイルの数だけ集計を繰り返す
        for (int i = 0; i < rcdFiles.size(); i++) {
            try {
            	File rcdfile = rcdFiles.get(i);
                // ファイルを読み込む準備）
                br = new BufferedReader(new FileReader(rcdfile));

                // ファイルの中身を溜めるためのリストを作成
                List<String> fileContents = new ArrayList<>();
                String line;

                // while文で最後まで読み込み、リストに入れる
                while ((line = br.readLine()) != null) {
                    fileContents.add(line);
                }

                // リストから1行目（支店コード）と2行目（売上金額）を取り出す
                String storeCode = fileContents.get(0);
                String salesAmount = fileContents.get(1);

                // 文字列だった売上額を、計算ができる数値（long型）に変換する
                long amount = Long.parseLong(salesAmount);

                // Mapから「今の合計金額」を取り出し、今回の売上を足して、Mapに保存し直す
                branchSales.put(storeCode, branchSales.get(storeCode) + amount);

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

            // 【エラー処理】ファイルが存在しない場合は、中断
            if (!file.exists()) {
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

                // 【エラー処理】
                // 要素（コードと名前）が2つない、または支店コードが3桁ではない場合は、中断
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