package com.gag.RuiwuYuexin.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.ibatis.type.*;
import java.sql.*;
import java.util.Collections;
import java.util.List;

@MappedJdbcTypes(JdbcType.OTHER)
@MappedTypes(List.class)
public class JsonListTypeHandler extends BaseTypeHandler<List<String>> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i,
                                    List<String> parameter, JdbcType jdbcType) throws SQLException {
        try {
            String json = MAPPER.writeValueAsString(parameter);
            ps.setObject(i, json, Types.VARCHAR);

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new SQLException("Error serializing List<String> to JSON", e);
        }
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parse(json);
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parse(json);
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parse(json);
    }

    private List<String> parse(String json) {
        if (json == null || json.isEmpty()) return Collections.emptyList();
        try {
            return MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new RuntimeException("JSON parse error.", e);
        }
    }
}