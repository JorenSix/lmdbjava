module lmdbjava.test {
    requires lmdbjava;
    requires org.hamcrest;
    requires junit;
    requires com.github.spotbugs.annotations;
    requires org.jnrproject.ffi;
    requires org.agrona.core;
    requires io.netty.buffer;
    requires com.google.common;

    exports org.lmdbjava.tests;
}