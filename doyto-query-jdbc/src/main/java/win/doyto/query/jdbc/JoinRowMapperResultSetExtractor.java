/*
 * Copyright © 2019-2022 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.jdbc;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import win.doyto.query.sql.JoinQueryBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JoinRowMapperResultSetExtractor
 *
 * @author f0rb on 2022-03-26
 */
@AllArgsConstructor
public class JoinRowMapperResultSetExtractor<I, R> implements ResultSetExtractor<Map<I, List<R>>> {
    private final Class<I> keyClass;
    private final RowMapper<R> rowMapper;

    public Map<I, List<R>> extractData(ResultSet rs) throws SQLException {
        Map<I, List<R>> results = new HashMap<>();
        int rowNum = 0;

        while (rs.next()) {
            I key = rs.getObject(JoinQueryBuilder.KEY_COLUMN, keyClass);
            R row = this.rowMapper.mapRow(rs, rowNum++);
            List<R> rows = results.computeIfAbsent(key, i -> new ArrayList<>());
            rows.add(row);
        }

        return results;
    }
}

