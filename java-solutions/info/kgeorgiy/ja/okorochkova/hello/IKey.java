package info.kgeorgiy.ja.okorochkova.hello;

interface IKey {
    default void writeOrRead() {}
    default void write() {}
    default void read() {}
}
