package com.example.tsukasa1260.speechrecognizer02;

import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.RequiresApi;

import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.widget.Toast;
import android.content.Intent;
import android.speech.RecognizerIntent;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private SpeechRecognizer sr;

    // 音声認識を開始する
    protected void startListening() {
        try {
            if (sr == null) {
                sr = SpeechRecognizer.createSpeechRecognizer(this);
                if (!SpeechRecognizer.isRecognitionAvailable(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), "音声認識が使えません",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                sr.setRecognitionListener(new listener());
            }
            // インテントの作成
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

            // 言語モデル指定
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); //LANGUAGE_MODEL_FREE_FORM, LANGUAGE_MODEL_WEB_SEARCH：認識結果をWEB検索に使う場合
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplicationContext().getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);

            /* Android Speech Recognizerは通常オンラインでgoogleのサーバと通信して音声認識を行う
             * デフォルトではオンライン認識・オフライン認識が自動的に切り替えられて認識
             * オフラインでの音声認識はモデルデータが必要
             * Android→言語と入力→Google音声入力→オフラインの音声認識でインストール済みの言語モデルの確認・別の言語のモデルをインストール
             * モデルなしの場合、ERROR_SERVER
             * API 23
             */
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true);

            sr.startListening(intent);

        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "startListening()でエラーが起こりました",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // 音声認識を終了する
    protected void stopListening() {
        if (sr != null) sr.destroy();
        sr = null;
    }

    // 音声認識を再開する
    public void restartListeningService() {
        stopListening();
        startListening();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();

        // RuntimePermissionの許可
        ArrayList<String> permissionList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }

        if(!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            requestPermissions(permissions, 0);
        }

        startListening();
    }

    @Override
    protected void onPause() {
        stopListening();
        super.onPause();
    }

    // RecognitionListenerの定義
    // 中が空でも全てのメソッドを書く必要がある
    class listener implements RecognitionListener {
        // 話し始めたときに呼ばれる
        public void onBeginningOfSpeech() {
            /*Toast.makeText(getApplicationContext(), "onBeginningofSpeech",
                    Toast.LENGTH_SHORT).show();*/
        }

        // 結果に対する反応などで追加の音声が来たとき呼ばれる
        // しかし呼ばれる保証はないらしい
        public void onBufferReceived(byte[] buffer) {
        }

        // 話し終わった時に呼ばれる
        public void onEndOfSpeech() {
            /*Toast.makeText(getApplicationContext(), "onEndofSpeech",
                    Toast.LENGTH_SHORT).show();*/
        }

        @Override
        // ネットワークエラーか認識エラーが起きた時に呼ばれる
        public void onError(int error) {
            String reason = "";
            switch (error) {
                // Audio recording error
                case SpeechRecognizer.ERROR_AUDIO:
                    reason = "ERROR_AUDIO";
                    break;
                // Other client side errors
                case SpeechRecognizer.ERROR_CLIENT:
                    reason = "ERROR_CLIENT";
                    break;
                // Insufficient permissions
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    reason = "ERROR_INSUFFICIENT_PERMISSIONS";
                    break;
                // 	Other network related errors
                case SpeechRecognizer.ERROR_NETWORK:
                    reason = "ERROR_NETWORK";
                    /* ネットワーク接続をチェックする処理をここに入れる */
                    break;
                // Network operation timed out
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    reason = "ERROR_NETWORK_TIMEOUT";
                    break;
                // No recognition result matched
                case SpeechRecognizer.ERROR_NO_MATCH:
                    reason = "ERROR_NO_MATCH";
                    break;
                // RecognitionService busy
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    reason = "ERROR_RECOGNIZER_BUSY";
                    break;
                // Server sends error status
                case SpeechRecognizer.ERROR_SERVER:
                    reason = "ERROR_SERVER";
                    /* ネットワーク接続をチェックをする処理をここに入れる */
                    break;
                // No speech input
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    reason = "ERROR_SPEECH_TIMEOUT";
                    break;
            }
            Toast.makeText(getApplicationContext(), reason, Toast.LENGTH_SHORT).show();
            restartListeningService();
        }

        // 将来の使用のために予約されている
        public void onEvent(int eventType, Bundle params) {
        }

        // 部分的な認識結果が利用出来るときに呼ばれる
        // 利用するにはインテントでEXTRA_PARTIAL_RESULTSを指定する必要がある
        public void onPartialResults(Bundle partialResults) {
        }

        // 音声認識の準備ができた時に呼ばれる
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(), "話してください",
                    Toast.LENGTH_SHORT).show();
        }

        // 認識結果が準備できた時に呼ばれる
        public void onResults(Bundle results) {
            // 結果をArrayListとして取得
            ArrayList<String> results_array = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
            // 取得した文字列を結合
            String resultsString = "";
            /*for (int i = 0; i < results.size(); i++) {
                resultsString += results_array.get(i) + "。";
            }*/
            resultsString = results_array.get(0);
            // トーストを使って結果表示
            Toast.makeText(getApplicationContext(), resultsString, Toast.LENGTH_LONG).show();
            restartListeningService();
        }

        // サウンドレベルが変わったときに呼ばれる
        // 呼ばれる保証はない
        public void onRmsChanged(float rmsdB) {
        }
    }
}