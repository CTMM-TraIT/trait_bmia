////////////////////////////////////////////////////////////////////////
//
// DateTimeFormatParser.java
//
// This file was generated by MapForce 2011r2sp1.
//
// YOU SHOULD NOT MODIFY THIS FILE, BECAUSE IT WILL BE
// OVERWRITTEN WHEN YOU RE-RUN CODE GENERATION.
//
// Refer to the MapForce Documentation for further details.
// http://www.altova.com/mapforce
//
////////////////////////////////////////////////////////////////////////


package com.altova.functions;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.altova.types.DateTime;

public class DateTimeFormatParser {

	public DateTimeFormatParser( final String sPattern)
	{
		mPattern = sPattern;
		mFieldOpen = '[';
		mFieldClose = ']';
	}

	public String formatDateTime( DateTime dt )
	{
		String sOutput = "";

		int nLength = mPattern.length();
		ParseState state = ParseState.NORMAL;
		ArgumentState argState = ArgumentState.COMPONENT;
		FieldInfo fieldInfo = new FieldInfo();

		for( int i = 0; i < nLength; ++i)
		{
			char cCurrent = mPattern.charAt(i);
			char cNext = i + 1 < nLength ? mPattern.charAt(i + 1) : 0;

			switch( state )
			{
				case NORMAL:
				{
					if( cCurrent == mFieldOpen )
					{
						if( cNext != mFieldOpen )
						{
							state = ParseState.INFIELD;
							argState = ArgumentState.COMPONENT;
							fieldInfo.reset();
						}
						else
						{
							sOutput += cCurrent;
							i++;
						}
					}
					else if( cCurrent == mFieldClose )
					{
						if( cNext != mFieldClose )
						{
							//error something is wrong with the pattern
							throw new IllegalArgumentException( "Incorrect pattern: '" + mPattern + "'");
						}
						else
						{
							sOutput += cCurrent;
							i++;
						}
					}
					else
					{
						sOutput += cCurrent;
					}
				}
				break;
				case INFIELD:
				{
					if( cCurrent == mFieldClose )
					{
						if( cNext != mFieldClose )
						{
							state = ParseState.NORMAL;

							if( fieldInfo.mComponent > 0 )
							{
								String sValue = getComponentValue(dt, fieldInfo.mComponent);
								sValue = processFormatModifier( dt, sValue, fieldInfo);
								sValue = processWidth( sValue, fieldInfo);

								sOutput += sValue;
							}
						}
						else
						{
							//error ]] in field
							throw new IllegalArgumentException( "Incorrect pattern: '" + mPattern + "'");
						}
					}
					else
					{
						switch( argState )
						{
							case COMPONENT:
							{
								fieldInfo.mComponent = cCurrent;
								argState = ArgumentState.FORMAT;
							}
							break;
							case FORMAT:
							{
								if( cCurrent == ',' )
									argState = ArgumentState.WIDTH;
								else
									fieldInfo.mModifier += cCurrent;
							}
							break;
							case WIDTH:
							{
								fieldInfo.mWidth += cCurrent;
							}
							break;
						}
					}
				}
				break;
			}
		}

		if( state != ParseState.NORMAL )
			throw new IllegalArgumentException( "Incorrect pattern: '" + mPattern + "'");

		return sOutput;
	}

	protected String processFormatModifier(
		final DateTime dtData,
		final String sValue,
		FieldInfo fieldInfo)
	{
		if( fieldInfo.mModifier.length() == 0 )
			return sValue;

		String sResult = sValue;
		int nZeroPad = 0;
		while( fieldInfo.mModifier.charAt(nZeroPad) == '0' ) nZeroPad++;

		if( fieldInfo.mModifier.substring(nZeroPad).equals("1") )
		{
			StringBuffer sbResult = new StringBuffer(sValue);
			if( fieldInfo.mComponent == 'F' )
			{
				sbResult = new StringBuffer();
				int day = dtData.getValue().get( Calendar.DAY_OF_WEEK);
				day = day == 1 ? 7 : day - 1; //strange calendar
				sbResult.append( day );
				fieldInfo.mComponent = 'D'; //prevent processWidth to handle the output as string
			}

			//add padding zeros
			int nLength = sbResult.length();
			if( sbResult.length() < nZeroPad + 1 )
				for( int i = 0; i < nZeroPad + 1 - nLength; i++) sbResult.insert(0, '0');
			sResult = sbResult.toString();
		}
		else if( fieldInfo.mModifier.equals("N")
				|| fieldInfo.mModifier.equals("n")
				|| fieldInfo.mModifier.equals("Nn") )
		{
			sResult = getComponentNameValue( dtData, fieldInfo);
			if( fieldInfo.mModifier.equals("N") )
				sResult = sResult.toUpperCase();
			else if( fieldInfo.mModifier.equals("n") )
				sResult = sResult.toLowerCase();
		}
		else
		{
			throw new IllegalArgumentException( "Unknown format modifier: '" + fieldInfo.mModifier + "'");
		}

		return sResult;
	}

	protected String processWidth(
		final String sValue,
		FieldInfo fieldInfo)
	{
		if( fieldInfo.mWidth.length() == 0 )
			return sValue;

		StringBuffer sResult = new StringBuffer(sValue);
		int nLength = sResult.length();

		fieldInfo.Analyze();

		if( fieldInfo.mMaxWidth > 0 )
		{
			if( fieldInfo.mMaxWidth >= fieldInfo.mMinWidth )
			{
				if( nLength > fieldInfo.mMaxWidth )
				{
					if( fieldInfo.mComponent != 'Y' )
						sResult.delete( fieldInfo.mMaxWidth, nLength );
					else
						sResult.delete( 0, nLength - fieldInfo.mMaxWidth );
				}
			}
		}

		if( fieldInfo.mMinWidth > nLength)
		{
			if( fieldInfo.mComponent != 'F' ) // F day of week, is currently the only text and should be padded with ' '
				for( int i = 0; i < fieldInfo.mMinWidth - nLength; i++) sResult.insert(0, '0');
			else
				for( int i = 0; i < fieldInfo.mMinWidth - nLength; i++) sResult.append(' ');
		}

		return sResult.toString();
	}


	protected String getComponentNameValue(
		DateTime dtData,
		FieldInfo fieldInfo)
	{
		String sValue;

		switch( fieldInfo.mComponent )
		{
			case 'M':
				sValue = MonthNames[dtData.getMonth() - 1];
				fieldInfo.mComponent = 'F'; //this allows the process width to handle the result as Text
			break;
			case 'D':
				sValue = DayNames[ dtData.getValue().get( Calendar.DAY_OF_WEEK) ];
				fieldInfo.mComponent = 'F'; //this allows the process width to handle the result as Text
			break;
			default:
				sValue = getComponentValue(dtData, fieldInfo.mComponent);
		}

		return sValue;
	}

	protected String getComponentValue( DateTime dtData, char cComponent)
	{
		StringBuffer sValue = new StringBuffer();

		switch( cComponent )
		{
			case 'd': sValue.append( dtData.getValue().get( Calendar.DAY_OF_YEAR) ); break;
			case 'D': sValue.append( dtData.getDay() ); break;
			case 'F': sValue.append( DayNames[ dtData.getValue().get( Calendar.DAY_OF_WEEK) ] ); break;
			case 'M': sValue.append( dtData.getMonth() ); break;
			case 'Y': sValue.append( dtData.getYear() ); break;
			case 'W':
			{
				Calendar cal = dtData.getValue();
				cal.setMinimalDaysInFirstWeek( 4);
				cal.setFirstDayOfWeek(Calendar.MONDAY);
				sValue.append(cal.get( Calendar.WEEK_OF_YEAR) );
			}
			break;
			case 'w':
			{
				Calendar cal = dtData.getValue();
				cal.setFirstDayOfWeek(Calendar.MONDAY);
				sValue.append(cal.get( Calendar.WEEK_OF_MONTH) );
			}
			break;
			case 'P': sValue.append( dtData.getHour() < 13 ? "A.M." : "P.M." ); break;
			case 'H': sValue.append( dtData.getHour() ); break;
			case 'h': sValue.append( ( dtData.getHour() > 12 ? dtData.getHour() - 12 : dtData.getHour() ) ); break;
			case 'm':
			{
				sValue.append( dtData.getMinute() );
				if( sValue.length() < 2)
					sValue.insert( 0, '0' );
			}
			break;
			case 's':
			{
				sValue.append( dtData.getSecond() );
				if( sValue.length() < 2)
					sValue.insert( 0, '0' );
			}
			break;
			case 'f':
			{
				sValue.append( (int)(dtData.getPartSecond() * 1000 ));
			}
			break;
			case 'z': sValue.append( "GMT" );
			case 'Z':
			{
				if( dtData.hasTimezone() != DateTime.TZ_MISSING)
				{
					StringBuffer hour = new StringBuffer();
					hour.append( Math.abs(dtData.getTimezoneOffset() / 60) );
					StringBuffer mins = new StringBuffer();
					mins.append( Math.abs(dtData.getTimezoneOffset() % 60) );
					sValue.append( dtData.getTimezoneOffset() >= 0 ? '+' : '-' );
					sValue.append( hour.length() < 2 ? '0' + hour.toString() : hour.toString() );
					sValue.append(':');
					sValue.append(mins.length() < 2 ? '0' + mins.toString() : mins.toString() );
				}
				else
					sValue.append("+00:00");
			}
			break;
			default:
				throw new IllegalArgumentException( "Unknown component specifier: '" + cComponent + "'");
		}
		return sValue.toString();
	}


	public DateTime parseDateTime( String sInput)
	{
		String sPattern = mPattern.trim();
		StringBuffer sbInput = new StringBuffer(sInput);
		ParseState state = ParseState.NORMAL;
		ArgumentState argState = ArgumentState.COMPONENT;
		FieldInfo fieldInfo = new FieldInfo();
		DateTimeData dt = new DateTimeData();

		int nLength = sPattern.length();
		for( int i = 0; i < nLength; ++i)
		{
			char cCurrent = sPattern.charAt(i);
			char cNext = i + 1 < nLength ? sPattern.charAt(i + 1) : 0;

			switch( state )
			{
				case NORMAL:
				{
					if( cCurrent == mFieldOpen )
					{
						if( cNext != mFieldOpen )
						{
							state = ParseState.INFIELD;
							argState = ArgumentState.COMPONENT;
							fieldInfo.reset();
						}
						else
						{
							i++;
							if( cCurrent == sbInput.charAt(0) )
							{
								sbInput.deleteCharAt(0);
							}
							else
							{
								throw new IllegalArgumentException( "Pattern not matching field opening '" + mFieldOpen + "'. Unable to read: '" + sbInput.toString() + "'");
							}
						}
					}
					else
					{
						if( cCurrent == sbInput.charAt(0) )
						{
							sbInput.deleteCharAt(0);
						}
						else
						{
							throw new IllegalArgumentException( "Pattern doesn't match! Unable to read: '" + sbInput.toString() + "'");
						}
					}
				}
				break;
				case INFIELD:
				{
					if( cCurrent == mFieldClose )
					{
						if( cNext != mFieldClose )
						{
							state = ParseState.NORMAL;

							if( fieldInfo.mComponent > 0 )
							{
								//Analyze field read min and max width
								fieldInfo.Analyze();
								//Read the field from input and apply correct value
								ReadField( dt, fieldInfo, sbInput, cNext);
							}
						}
						else
						{
							i++;
						}
					}
					else
					{
						switch( argState )
						{
							case COMPONENT:
							{
								fieldInfo.mComponent = cCurrent;
								argState = ArgumentState.FORMAT;
							}
							break;
							case FORMAT:
							{
								if( cCurrent == ',' )
									argState = ArgumentState.WIDTH;
								else
									fieldInfo.mModifier += cCurrent;
							}
							break;
							case WIDTH:
							{
								fieldInfo.mWidth += cCurrent;
							}
							break;
						}
					}
				}
				break;
			}
		}

		int[] monthTable = new GregorianCalendar().isLeapYear( dt.Year ) ? monthStartLeap : monthStart;
		if( dt.DayOfYear > 0 && dt.DayOfYear <= monthTable[12] )
		{
			int i = 12;
			while( monthTable[i] >= dt.DayOfYear ) i--;
			dt.Day = dt.DayOfYear - monthTable[i];
			dt.Month = i + 1;
		}

		DateTime result = new DateTime();
		result.setMonth(dt.Month);
		result.setYear(dt.Year);
		result.setDay(dt.Day);
		result.setHour(dt.Hour);
		result.setMinute(dt.Minute);
		result.setSecond(dt.Second);
		result.setPartSecond(dt.MilliSecond);
		if( dt.TimeZone != 0)
			result.setHasTimezone(DateTime.TZ_OFFSET);
		result.setTimezoneOffset(dt.TimeZone);
		return result;
	}

	boolean StringToRead( String sModifier)
	{
		if( sModifier.equals("N") || sModifier.equals("n") || sModifier.equals("Nn") )
			return true;
		else
			return false;
	}

	int ReadNumber( StringBuffer sbInput, int nMax )
	{
		StringBuffer sbNumber = new StringBuffer("");
		sbInput = Lang.trimLeft(sbInput, " t"); //trim beginning spaces
		int i = 0;

		nMax = nMax == 0 ? sbInput.length() : nMax;
		for (;i < nMax && Character.isDigit( sbInput.charAt(i) ); ++i)
		{
			sbNumber.append(sbInput.charAt(i));
		}
		sbInput.delete(0, i);

		int ret;
		try {
			ret = Integer.parseInt( sbNumber.toString());
		}
		catch (NumberFormatException e) {
			ret = 0;
		}
		return ret;
	}

	private String ReadStringUntil( StringBuffer sbInput, int nMax, char cUntil )
	{
		StringBuffer sbRead = new StringBuffer("");
		int i = 0;

		nMax = nMax == 0 ? sbInput.length() : nMax;
		for (;i < nMax && sbInput.charAt(i) != cUntil; ++i)
		{
			sbRead.append( sbInput.charAt(i) );
		}
		sbInput.delete(0, i);

		return sbRead.toString();
	}

	private void ReadField(
		DateTimeData dtData,
		FieldInfo fieldinfo,
		StringBuffer sbInput,
		char cNext)
	{
		switch( fieldinfo.mComponent )
		{
		case 'd': //day of the year
		{
			dtData.DayOfYear = ReadNumber(sbInput, fieldinfo.mMaxWidth);
		}
		break;

		case 'D':
		{
			if( !StringToRead(fieldinfo.mModifier) )
				dtData.Day = ReadNumber(sbInput, fieldinfo.mMaxWidth);
		}
		break;
		case 'M':
		{
			String sInputBackup = sbInput.toString();
			if( !StringToRead(fieldinfo.mModifier) )
				dtData.Month = ReadNumber(sbInput, fieldinfo.mMaxWidth);
			else
			{
				String sMonth = ReadStringUntil( sbInput, fieldinfo.mMaxWidth, cNext);

				//find the month from the table
				int i = 0;
				int nMax = fieldinfo.mMaxWidth == 0 ? sMonth.length() : fieldinfo.mMaxWidth;
				while( i < MonthNames.length
					&& !sMonth.equalsIgnoreCase(
						MonthNames[i].substring(
							0,
							nMax > MonthNames[i].length() ? MonthNames[i].length() : nMax
						)
					)
				) i++;


				dtData.Month = i + 1;
			}

			if( dtData.Month > 12)
			{
				throw new IllegalArgumentException( "Pattern component '" + fieldinfo.mComponent + "' doesn't match! Unable to read: '" + sInputBackup + "'");
			}
		}
		break;
		case 'Y':
		{
			dtData.Year = ReadNumber(sbInput, fieldinfo.mMaxWidth);
			if( fieldinfo.mMaxWidth == 2)
			{
				 //change this in the year 2050;
				if( dtData.Year > 50)
					dtData.Year += 1900;
				else
					dtData.Year += 2000;
			}
		}
		break;
		case 'P':
		{
			String sPM = ReadStringUntil( sbInput, 4, (char)0);
			dtData.Hour += sPM.equalsIgnoreCase( "p.m.") ? 12 : 0;
		}
		break;
		case 'h':
		case 'H':
		{
			dtData.Hour = ReadNumber(sbInput, fieldinfo.mMaxWidth);
		}
		break;
		case 'm':
		{
			dtData.Minute = ReadNumber(sbInput, fieldinfo.mMaxWidth);
		}
		break;
		case 's':
		{
			dtData.Second = ReadNumber(sbInput, fieldinfo.mMaxWidth);
		}
		break;
		case 'f':
		{
			int nMilli = ReadNumber(sbInput, fieldinfo.mMaxWidth);
			dtData.MilliSecond = nMilli / Math.pow( 10.0f, (nMilli + "").length());
		}
		break;
		case 'z':
		{
			if( sbInput.toString().startsWith( "GMT" ) )
				sbInput.delete(0, 3);
			else
				throw new IllegalArgumentException( "Pattern component '" + fieldinfo.mComponent + "' doesn't match! Unable to read: '" + sbInput + "'");
		}
		case 'Z':
		{
			int nMinus = 1;
			if( sbInput.charAt(0) == '-' )
				nMinus = -1;
			sbInput.deleteCharAt(0);

			int nHour = ReadNumber(sbInput, 2);

			if( sbInput.charAt(0) != ':' )
			{
				//error
				throw new IllegalArgumentException( "Pattern component '" + fieldinfo.mComponent + "' doesn't match! Unable to read: '" + sbInput + "'");
			}
			else
				sbInput.deleteCharAt(0);

			int nMinutes = ReadNumber(sbInput, 2);

			dtData.TimeZone = (nHour * 60 + nMinutes) * nMinus;
		}
		break;
		case 'i':
		case 'I':
		{
			ReadStringUntil(sbInput, fieldinfo.mMaxWidth, cNext);
		}
		break;
		default:
			throw new IllegalArgumentException( "Unknown component specifier: '" + fieldinfo.mComponent + "'");
		}
	}


	private int monthStart[] = { 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365 };
	private int monthStartLeap[] = { 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366 };

	private String[] DayNames = {
		"",
		"Sunday",
		"Monday",
		"Tuesday",
		"Wednesday",
		"Thursday",
		"Friday",
		"Saturday"
	};

	private String[] MonthNames = {
		"January",
		"February",
		"March",
		"April",
		"May",
		"June",
		"July",
		"August",
		"September",
		"October",
		"November",
		"December"
	};

	protected enum ParseState {
		NORMAL,
		INFIELD
	}

	protected enum ArgumentState {
		COMPONENT,
		FORMAT,
		WIDTH
	}

	private class FieldInfo {
		public char mComponent;
		public String mModifier;
		public String mWidth;
		public int mMinWidth;
		public int mMaxWidth;

		public FieldInfo() {
			reset();
		}

		public void reset() {
			mComponent = 0;
			mModifier = "";
			mWidth = "";
			mMinWidth = 0;
			mMaxWidth = 0;
		}

		public boolean Analyze() {
			int minusPos = mWidth.indexOf( '-' );
			if( minusPos == -1 )
			{
				try {
					mMinWidth = Integer.parseInt( mWidth );
				}
				catch (NumberFormatException e) {
					mMinWidth = 0;
				}
			}
			else
			{
				try {
					mMinWidth = Integer.parseInt( mWidth.substring(0, minusPos) );
				}
				catch (NumberFormatException e) {
					mMinWidth = 0;
				}
				try {
					mMaxWidth = Integer.parseInt( mWidth.substring( minusPos + 1 ) );
				}
				catch (NumberFormatException e) {
					mMaxWidth = 0;
				}
			}

			return true;
		}
	}

	class DateTimeData
	{
		public DateTimeData() {
			Day = 1;
			Month = 1;
			Year = 0;
			Hour = 0;
			Minute = 0;
			Second = 0;
			TimeZone = com.altova.types.CalendarBase.TZ_MISSING;
			DayOfYear = 0;
		}

		int Day;
		int Month;
		int Year;
		int Hour;
		int Minute;
		int Second;
		double MilliSecond;
		int TimeZone;

		int DayOfYear; //this value is stored for later processing
	};

	private String mPattern;
	private char mFieldOpen;
	private char mFieldClose;
}

