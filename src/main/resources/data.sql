-- 애플리케이션 시작 시 삽입되는 테스트용 더미 데이터

-- 대회 참가자 12명. 모두 팀 미배정 상태이며, POST /team 으로 5명씩 팀을 자동 배정할 수 있다.
-- 12명이면 5명 팀 2개가 만들어지고, 나중에 등록된 2명은 미배정으로 남는다.
-- 모든 더미 유저의 로그인 비밀번호는 test1234 이다. password 컬럼에는 BCrypt로 암호화한 값을 넣는다.
INSERT INTO users (student_num, name, game_nickname, tier, password) VALUES
    ('3101', '김하늘', 'SkyKim', 'CHALLENGER', '$2a$10$VdbQG927dDRSyOygeeW16eP8NSiND82HjeOT3Fr1xsVtWmmcvubyK'),
    ('3102', '이도윤', 'DoYoon2', 'DIAMOND', '$2a$10$VdbQG927dDRSyOygeeW16eP8NSiND82HjeOT3Fr1xsVtWmmcvubyK'),
    ('3103', '한지호', 'Jiho3', 'PLATINUM', '$2a$10$VdbQG927dDRSyOygeeW16eP8NSiND82HjeOT3Fr1xsVtWmmcvubyK'),
    ('3104', '서예린', 'Yerin', 'GOLD', '$2a$10$VdbQG927dDRSyOygeeW16eP8NSiND82HjeOT3Fr1xsVtWmmcvubyK'),
    ('2101', '박서준', 'SeoJunP', 'MASTER', '$2a$10$VdbQG927dDRSyOygeeW16eP8NSiND82HjeOT3Fr1xsVtWmmcvubyK'),
    ('2102', '최지우', 'JiwooC', 'EMERALD', '$2a$10$VdbQG927dDRSyOygeeW16eP8NSiND82HjeOT3Fr1xsVtWmmcvubyK'),
    ('2103', '윤태양', 'SunYoon', 'GOLD', '$2a$10$VdbQG927dDRSyOygeeW16eP8NSiND82HjeOT3Fr1xsVtWmmcvubyK'),
    ('2104', '강다은', 'DaEun', 'SILVER', '$2a$10$VdbQG927dDRSyOygeeW16eP8NSiND82HjeOT3Fr1xsVtWmmcvubyK'),
    ('1101', '정민재', 'MinJae5', 'DIAMOND', '$2a$10$VdbQG927dDRSyOygeeW16eP8NSiND82HjeOT3Fr1xsVtWmmcvubyK'),
    ('1102', '오세훈', 'SeHunO', 'PLATINUM', '$2a$10$VdbQG927dDRSyOygeeW16eP8NSiND82HjeOT3Fr1xsVtWmmcvubyK'),
    ('1103', '임수아', 'SuaLim', 'BRONZE', '$2a$10$VdbQG927dDRSyOygeeW16eP8NSiND82HjeOT3Fr1xsVtWmmcvubyK'),
    ('1104', '조현우', 'HyunWoo', 'IRON', '$2a$10$VdbQG927dDRSyOygeeW16eP8NSiND82HjeOT3Fr1xsVtWmmcvubyK');
