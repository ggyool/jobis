# ERD (Entity Relationship Diagram)

## 테이블 상세 정보

### activity
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | LONG | PRIMARY KEY, AUTO INCREMENT | 활동 고유 ID |
| started_at | DATETIME | NOT NULL | 활동 시작 시간 |
| ended_at | DATETIME | NULL | 활동 종료 시간 |
| description | TEXT | NULL | 활동 설명 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 레코드 생성 시간 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 레코드 수정 시간 |