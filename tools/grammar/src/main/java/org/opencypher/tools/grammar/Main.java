package org.opencypher.tools.grammar;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.BiFunction;

import org.opencypher.grammar.Grammar;

import static java.lang.String.format;

interface Main extends Serializable
{
    static void main( String... args ) throws Throwable
    {
        if ( args == null || args.length < 1 )
        {
            String path = pathOf( Main.class );
            if ( new File( path ).isFile() && path.endsWith( ".jar" ) )
            {
                System.err.printf( "USAGE: java -jar %s <tool> ...%n", path );
            }
            else
            {
                System.err.printf( "USAGE: java -cp %s %s <tool> ...%n", path, Main.class.getName() );
            }
            System.exit( 1 );
        }
        else
        {
            Method main;
            try
            {
                Class<?> cls = Class.forName( Main.class.getPackage().getName() + '.' + args[0] );
                main = cls.getDeclaredMethod( "main", String[].class );
                if ( !Modifier.isStatic( main.getModifiers() ) || "Main".equals( args[0] ) )
                {
                    throw new IllegalArgumentException( args[0] );
                }
            }
            catch ( Exception e )
            {
                System.err.println( "Unknown formatter: " + args[0] );
                throw e;
            }
            try
            {
                main.invoke( null, (Object) Arrays.copyOfRange( args, 1, args.length ) );
            }
            catch ( InvocationTargetException e )
            {
                throw e.getTargetException();
            }
        }
    }

    void write( Grammar grammar, OutputStream out ) throws Exception;

    static void execute( Main program, String... args ) throws Exception
    {
        if ( args.length == 1 )
        {
            try ( FileInputStream input = new FileInputStream( args[0] ) )
            {
                program.write( Grammar.parseXML( input ), System.out );
            }
        }
        else
        {
            System.err.println(
                    program.usage( ( cp, cls ) -> format( "USAGE: java -cp %s %s <grammar.xml>%n", cp, cls ) ) );
            System.exit( 1 );
        }
    }

    default String usage( BiFunction<String, String, String> usage )
    {
        try
        {
            Method replace = getClass().getDeclaredMethod( "writeReplace" );
            replace.setAccessible( true );
            SerializedLambda lambda = (SerializedLambda) replace.invoke( this );
            String className = lambda.getImplClass().replace( '/', '.' );
            Class<?> implClass = Class.forName( className );
            String path = pathOf( implClass );
            return usage.apply( path, className );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    static String pathOf( Class<?> cls )
    {
        return cls.getProtectionDomain().getCodeSource().getLocation().getPath();
    }
}
