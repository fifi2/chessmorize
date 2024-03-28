package io.github.fifi2.chessmorize.helper;

public abstract class Constants {

    public abstract static class Api {
        private static final String API = "/api";
        public static final String BOOKS = API + "/book";
        public static final String BOOK = BOOKS + "/{bookId}";
        private static final String TRAINING = API + "/training/book/{bookId}";
        public static final String SET_RESULT = TRAINING + "/set-result";
        public static final String NEXT_LINE = TRAINING + "/next-line";
        public static final String NEXT_SLOT = TRAINING + "/next-slot";
    }

    public abstract static class Json {

        // json tools
        private static final String ROOT = "$";
        private static final String SIZE = ".length()";
        private static final String IDX = "[%d]";

        // book
        public static final String ID = ROOT + ".id";
        public static final String STUDY_ID = ROOT + ".studyId";
        public static final String NAME = ROOT + ".name";
        public static final String CHAPTERS = ROOT + ".chapters";
        public static final String CHAPTERS_SIZE = CHAPTERS + SIZE;
        private static final String LINES = ROOT + ".lines";
        public static final String LINES_SIZE = LINES + SIZE;
        public static final String CALENDAR_SLOT = ROOT + ".calendarSlot";

        // chapters
        private static final String CHAPTER = CHAPTERS + IDX;
        public static final String CHAPTER_ID = CHAPTER + ".id";
        public static final String CHAPTER_TITLE = CHAPTER + ".title";
        private static final String CHAPTER_MOVES = CHAPTER + ".nextMoves";
        public static final String CHAPTER_MOVES_SIZE = CHAPTER_MOVES + SIZE;

        // line fields
        private static final String _LINE_ID = ".id";
        private static final String _LINE_CHAPTER_ID = ".chapterId";
        private static final String _LINE_MOVES = ".moves";
        private static final String _LINE_MOVE_ID = ".moveId";
        private static final String _LINE_MOVE_UCI = ".uci";
        private static final String _LINE_BOX_ID = ".boxId";
        private static final String _LINE_LAST_TRAINING = ".lastTraining";
        private static final String _LINE_LAST_CALENDAR_SLOT =
            ".lastCalendarSlot";

        // line
        private static final String LINE = LINES + IDX;
        public static final String LINE_ID = LINE + _LINE_ID;
        public static final String LINE_CHAPTER_ID = LINE + _LINE_CHAPTER_ID;
        private static final String LINE_MOVES = LINE + _LINE_MOVES;
        public static final String LINE_MOVES_SIZE = LINE_MOVES + SIZE;
        private static final String LINE_MOVE = LINE_MOVES + IDX;
        public static final String LINE_MOVE_ID = LINE_MOVE + _LINE_MOVE_ID;
        public static final String LINE_MOVE_UCI = LINE_MOVE + _LINE_MOVE_UCI;
        public static final String LINE_BOX_ID = LINE + _LINE_BOX_ID;
        public static final String LINE_LAST_TRAINING =
            LINE + _LINE_LAST_TRAINING;
        public static final String LINE_LAST_CALENDAR_SLOT =
            LINE + _LINE_LAST_CALENDAR_SLOT;

        // next line
        public static final String NEXT_LINE_ID = ROOT + _LINE_ID;
        public static final String NEXT_LINE_CHAPTER_ID =
            ROOT + _LINE_CHAPTER_ID;
        private static final String NEXT_LINE_MOVES = ROOT + _LINE_MOVES;
        public static final String NEXT_LINE_MOVES_SIZE =
            NEXT_LINE_MOVES + SIZE;
        private static final String NEXT_LINE_MOVE = NEXT_LINE_MOVES + IDX;
        public static final String NEXT_LINE_MOVE_UCI = NEXT_LINE_MOVE + _LINE_MOVE_UCI;
        public static final String NEXT_LINE_BOX_ID = ROOT + _LINE_BOX_ID;
        public static final String NEXT_LINE_LAST_TRAINING =
            ROOT + _LINE_LAST_TRAINING;
        public static final String NEXT_LINE_LAST_CALENDAR_SLOT =
            ROOT + _LINE_LAST_CALENDAR_SLOT;

        public static String MOVE(final int chapter,
                                  final int... move) {

            final StringBuilder jsonPathMoves = new StringBuilder();
            for (int mIdx : move) {
                jsonPathMoves.append(".nextMoves[").append(mIdx).append("]");
            }
            return String.format(CHAPTER, chapter) + jsonPathMoves;
        }

    }

}
