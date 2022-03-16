## ビルド
```
mvn compile
```

## デプロイ
```
gcloud functions deploy java-function --entry-point CloudFunctionsForJava --runtime java11 --trigger-http --memory 256MB --allow-unauthenticated
```

## ローカル実行
```
mvn function:run
```

動作確認
```
curl localhost:8080
```
