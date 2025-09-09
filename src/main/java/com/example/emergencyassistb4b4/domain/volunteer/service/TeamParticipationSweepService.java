package com.example.emergencyassistb4b4.domain.volunteer.service;

import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TeamParticipationSweepService {

    private final RedisTemplate<String, Object> rt;
    private final VolunteerTeamRepository teamRepo;
    private static final RedisSerializer<String> STR = StringRedisSerializer.UTF_8;
    private static final List<String> PATTERNS = List.of(
            "team:*:*:users",
            "team:*:*:count",
            "team:*:*:*:duplicate"
    );

    public void sweepOnce(int scanCount, int dbBatch) {

        Set<String> keys = scanAll(PATTERNS, scanCount);
        if (keys.isEmpty()) return;

        // 키에서 teamId 추출: 끝 토큰이 teamId 라고 가정
        Map<String, Long> keyToTeam = new HashMap<>();
        for (String k : keys) {
            Long id = parseTeamId(k);
            if (id != null) keyToTeam.put(k, id);
        }
        if (keyToTeam.isEmpty()) return;

        // 배치로 존재 팀 조회
        List<Long> ids = new ArrayList<>(new HashSet<>(keyToTeam.values()));
        Set<Long> valid = new HashSet<>();
        for (int i = 0; i < ids.size(); i += dbBatch) {
            valid.addAll(teamRepo.findExistingIdsIn(ids.subList(i, Math.min(i+dbBatch, ids.size()))));
        }
        Set<Long> invalid = new HashSet<>(keyToTeam.values()); invalid.removeAll(valid);
        if (invalid.isEmpty()) return;

        List<String> del = keyToTeam.entrySet().stream()
                .filter(e -> invalid.contains(e.getValue()))
                .map(Map.Entry::getKey).toList();

        rt.executePipelined((RedisCallback<Object>) c -> {
            for (String k : del) c.keyCommands().unlink(STR.serialize(k));
            return null;
        });
    }

    private Set<String> scanAll(List<String> patterns, int count) {

        Set<String> out = new HashSet<>();
        for (String p : patterns) out.addAll(scan(p, count));

        return out;
    }
    private Set<String> scan(String pattern, int count) {

        Set<String> out = new HashSet<>();
        ScanOptions opt = ScanOptions.scanOptions().match(pattern).count(count).build();

        rt.execute((RedisConnection conn) -> {
            Cursor<byte[]> cur = conn.keyCommands().scan(opt);
            try { while (cur.hasNext()) {
                String k = STR.deserialize(cur.next());
                if (k != null) out.add(k);
            }} finally { try { cur.close(); } catch (Exception ignore) {} }
            return null;
        });

        return out;
    }
    private static Long parseTeamId(String key) {

        // key 형식: team:{postId}:{teamId}(:...) → index 2가 teamId
        String[] toks = key.split(":");
        if (toks.length < 3) return null;
        String s = toks[2];
        for (int j = 0; j < s.length(); j++) if (!Character.isDigit(s.charAt(j))) return null;
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }
}


