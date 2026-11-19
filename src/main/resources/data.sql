-- 애플리케이션 시작 시 삽입되는 테스트용 더미 데이터

INSERT INTO team (name) VALUES
    ('알고리즘 마스터즈'),
    ('바인드 브레이커스'),
    ('널 포인터즈'),
    ('세그폴트');

-- 모든 더미 유저의 로그인 비밀번호는 test1234 이다. password 컬럼에는 BCrypt로 암호화한 값을 넣는다.
-- team_id는 위에서 삽입한 팀의 순서(1~4)를 따른다. 세그폴트(4)는 선수가 없는 팀이다.
INSERT INTO users (student_num, name, game_nickname, tier, password, team_id) VALUES
    ('3101', '김하늘', 'SkyKim', 'DIAMOND', '$2a$10$VdbQG927dDRSyOygeeW16eP8NSiND82HjeOT3Fr1xsVtWmmcvubyK', 1),
    ('3102', '이도윤', 'DoYoon2', 'PLATINUM', '$2a$10$VdbQG927dDRSyOygeeW16eP8NSiND82HjeOT3Fr1xsVtWmmcvubyK', 1),
    ('2103', '박서준', 'SeoJunP', 'GOLD', '$2a$10$VdbQG927dDRSyOygeeW16eP8NSiND82HjeOT3Fr1xsVtWmmcvubyK', 2),
    ('2104', '최지우', 'JiwooC', 'GOLD', '$2a$10$VdbQG927dDRSyOygeeW16eP8NSiND82HjeOT3Fr1xsVtWmmcvubyK', 2),
    ('1105', '정민재', 'MinJae5', 'SILVER', '$2a$10$VdbQG927dDRSyOygeeW16eP8NSiND82HjeOT3Fr1xsVtWmmcvubyK', 3);
