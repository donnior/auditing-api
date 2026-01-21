```
curl -X POST "http://localhost:8080/auditing-api/chat-data/sync?startTime=2026-01-18T00:00:00%2B08:00&endTime=2026-01-18T00:01:59%2B08:00"

```


curl -X POST \
-H "Content-Type: application/json" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc2ODgxMDY4NiwiZXhwIjoxNzY5NDE1NDg2fQ.eWtssGFzrP37WLXlk4Umg1hGM2aDAD4DOv71ddhID0M" \
"http://localhost:8080/auditing-api/chat-data/sync?
startTime=2026-01-18T00:00:00%2B08:00&endTime=2026-01-18T00:01:59%2B08:00" \


curl -X POST \
-H "Content-Type: application/json" \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc2ODgxMDY4NiwiZXhwIjoxNzY5NDE1NDg2fQ.eWtssGFzrP37WLXlk4Umg1hGM2aDAD4DOv71ddhID0M" \
"https://zj.xingcanai.com/auditing-api/chat-data/sync?startTime=2026-01-15T23:59:58%2B08:00&endTime=2026-01-16T23:59:59%2B08:00"


```bash
# 外部接口：按更新时间增量获取在读学员课程周序列表（page/limit 必填，updateStartTime/updateEndTime 选填）
curl -X POST \
  -H "Content-Type: application/json" \
  -H "X-Token: VoyT09nB2fSlDGbF+NWzPxekKY1ZI/jqDKECSiTK6GoeoyLCARG9SaglnEvSG/WQ" \
  "https://www.cdxwsuger.cn/prod-api/api/wx/getCardUserList" \
  -d '{
    "page": 1,
    "limit": 100,
    "updateStartTime": "2026-01-15 11:45:00",
    "updateEndTime": "2026-01-15 23:45:00"
  }'
```

curl 'https://zj.xingcanai.com/auditing-api/report-job-test/daily?target_date=2026-01-11' \
  -H 'Accept: application/json, text/plain, */*' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc2ODgxMDY4NiwiZXhwIjoxNzY5NDE1NDg2fQ.eWtssGFzrP37WLXlk4Umg1hGM2aDAD4DOv71ddhID0M'
