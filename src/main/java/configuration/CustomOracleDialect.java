package configuration;

import org.hibernate.dialect.OracleDialect;

@SuppressWarnings("unused")
public class CustomOracleDialect extends OracleDialect {

	@Override
	public String getQuerySequencesString() {
		return "select SEQUENCE_OWNER, SEQUENCE_NAME, greatest(MIN_VALUE,         -9223372036854775807) MIN_VALUE,\n"
				+ "Least(MAX_VALUE, 9223372036854775808) MAX_VALUE, INCREMENT_BY,     CYCLE_FLAG, ORDER_FLAG, CACHE_SIZE,\n"
				+ "Least(greatest(LAST_NUMBER, -9223372036854775807), 9223372036854775808) LAST_NUMBER,\n"
				+ "SESSION_FLAG, KEEP_VALUE\n" + "from all_sequences";
	}

}