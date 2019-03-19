/*
 * Copyright (c) 2012-2019 Snowflake Computing Inc. All right reserved.
 */
package net.snowflake.client.jdbc;

import net.snowflake.client.AbstractDriverIT;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types;
import java.util.Calendar;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration tests for binding variable
 */
@RunWith(Theories.class)
public class BindingDataIT extends AbstractDriverIT
{
  @DataPoints
  public static short[] shortValues = {0, 1, -1, Short.MIN_VALUE, Short.MAX_VALUE};

  @Theory
  public void testBindShort(short shortValue) throws SQLException
  {
    Connection connection = getConnection();
    Statement statement = connection.createStatement();
    statement.execute("create or replace table test_bind_short(c1 number)");

    PreparedStatement preparedStatement = connection.prepareStatement(
        "insert into test_bind_short values (?)");
    preparedStatement.setShort(1, shortValue);
    preparedStatement.executeUpdate();

    preparedStatement = connection.prepareStatement(
        "select * from test_bind_short where c1 = ?");
    preparedStatement.setShort(1, shortValue);

    ResultSet resultSet = preparedStatement.executeQuery();
    assertThat(resultSet.next(), is(true));
    assertThat(resultSet.getShort("C1"), is(shortValue));

    resultSet.close();
    preparedStatement.close();

    statement.execute("drop table if exists test_bind_short");
    connection.close();
  }

  @DataPoints
  public static int[] intValues = {0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE};

  @Theory
  public void testBindInt(int intValue) throws SQLException
  {
    Connection connection = getConnection();
    Statement statement = connection.createStatement();
    statement.execute("create or replace table test_bind_int(c1 number)");

    PreparedStatement preparedStatement = connection.prepareStatement(
        "insert into test_bind_int values (?)");
    preparedStatement.setInt(1, intValue);
    preparedStatement.executeUpdate();

    preparedStatement = connection.prepareStatement(
        "select * from test_bind_int where c1 = ?");
    preparedStatement.setInt(1, intValue);

    ResultSet resultSet = preparedStatement.executeQuery();
    assertThat(resultSet.next(), is(true));
    assertThat(resultSet.getInt("C1"), is(intValue));

    resultSet.close();
    preparedStatement.close();

    statement.execute("drop table if exists test_bind_int");
    connection.close();
  }

  @DataPoints
  public static byte[] byteValues = {0, 1, -1, Byte.MAX_VALUE,
      Byte.MIN_VALUE};

  @Theory
  public void testBindByte(byte byteValue) throws SQLException
  {
    Connection connection = getConnection();
    Statement statement = connection.createStatement();
    statement.execute("create or replace table test_bind_byte(c1 integer)");

    PreparedStatement preparedStatement = connection.prepareStatement(
        "insert into test_bind_byte values (?)");
    preparedStatement.setByte(1, byteValue);
    preparedStatement.executeUpdate();

    preparedStatement = connection.prepareStatement(
        "select * from test_bind_byte where c1 = ?");
    preparedStatement.setInt(1, byteValue);

    ResultSet resultSet = preparedStatement.executeQuery();
    assertThat(resultSet.next(), is(true));
    assertThat(resultSet.getByte("C1"), is(byteValue));

    resultSet.close();
    preparedStatement.close();

    statement.execute("drop table if exists test_bind_byte");
    connection.close();
  }

  @Test
  public void testBindNull() throws SQLException
  {
    Connection connection = getConnection();
    Statement statement = connection.createStatement();
    statement.execute("create or replace table test_bind_null(id number, val " +
                          "number)");

    PreparedStatement preparedStatement = connection.prepareStatement(
        "insert into test_bind_null values (?, ?)");
    preparedStatement.setInt(1, 0);
    preparedStatement.setBigDecimal(2, null);
    preparedStatement.addBatch();

    preparedStatement.setInt(1, 1);
    preparedStatement.setNull(1, Types.INTEGER);
    preparedStatement.addBatch();

    preparedStatement.setInt(1, 2);
    preparedStatement.setObject(1, null, Types.BIGINT);
    preparedStatement.addBatch();

    preparedStatement.executeBatch();

    ResultSet rs = statement.executeQuery("select * from test_bind_null " +
                                              "order by id asc");
    int count = 0;
    while(rs.next())
    {
      assertThat(rs.getBigDecimal("VAL"), is(nullValue()));
      count ++;
    }

    assertThat(count, is(3));

    rs.close();
    preparedStatement.close();

    statement.execute("drop table if exists test_bind_null");
    connection.close();
  }

  @DataPoints
  public static Time[] timeValues = {
      Time.valueOf("00:00:00"),
      Time.valueOf("12:34:56"),
      Time.valueOf("12:00:00"),
      Time.valueOf("11:59:59"),
      Time.valueOf("15:30:00"),
      Time.valueOf("13:01:01"),
      Time.valueOf("12:00:00"),
  };

  @Theory
  public void testBindTime(Time timeVal) throws SQLException
  {
    Connection connection = getConnection();
    Statement statement = connection.createStatement();
    statement.execute("create or replace table test_bind_time(c1 time)");

    PreparedStatement preparedStatement = connection.prepareStatement(
        "insert into test_bind_time values (?)");
    preparedStatement.setTime(1, timeVal);
    preparedStatement.executeUpdate();

    preparedStatement = connection.prepareStatement(
        "select * from test_bind_time where c1 = ?");
    preparedStatement.setTime(1, timeVal);

    ResultSet resultSet = preparedStatement.executeQuery();
    assertThat(resultSet.next(), is(true));
    assertThat(resultSet.getTime("C1"), is(timeVal));

    resultSet.close();
    preparedStatement.close();

    statement.execute("drop table if exists test_bind_time");
    connection.close();
  }

  /**
   * Bind time with calendar is not supported now. Everything is in UTC, need
   * to revisit in the future
   */
  @Theory
  public void testBindTimeWithCalendar(Time timeVal) throws SQLException
  {
    Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    Calendar laCal = Calendar.getInstance(TimeZone.getTimeZone("PST"));

    Connection connection = getConnection();
    Statement statement = connection.createStatement();
    statement.execute("create or replace table test_bind_time_calendar(c1 " +
                          "time)");

    PreparedStatement preparedStatement = connection.prepareStatement(
        "insert into test_bind_time_calendar values (?)");
    preparedStatement.setTime(1, timeVal, laCal);
    preparedStatement.executeUpdate();

    // bind time with UTC
    preparedStatement = connection.prepareStatement(
        "select * from test_bind_time_calendar where c1 = ?");
    preparedStatement.setTime(1, timeVal, laCal);

    ResultSet resultSet = preparedStatement.executeQuery();
    assertThat(resultSet.next(), is(true));
    assertThat(resultSet.getTime("C1", utcCal), is(timeVal));

    resultSet.close();
    preparedStatement.close();

    statement.execute("drop table if exists test_bind_time_calendar");
    connection.close();
  }

  @Theory
  public void testBindTimeViaSetObject(Time timeVal) throws SQLException
  {
    Connection connection = getConnection();
    Statement statement = connection.createStatement();
    statement.execute("create or replace table test_bind_time(c1 time)");

    PreparedStatement preparedStatement = connection.prepareStatement(
        "insert into test_bind_time values (?)");
    preparedStatement.setObject(1, timeVal, Types.TIME);
    preparedStatement.executeUpdate();

    preparedStatement = connection.prepareStatement(
        "select * from test_bind_time where c1 = ?");
    preparedStatement.setObject(1, timeVal, Types.TIME);

    ResultSet resultSet = preparedStatement.executeQuery();
    assertThat(resultSet.next(), is(true));
    assertThat(resultSet.getTime("C1"), is(timeVal));

    resultSet.close();
    preparedStatement.close();

    statement.execute("drop table if exists test_bind_time");
    connection.close();
  }
}