package edu.kit.tm.pseprak2.alushare.network.coding.decodestates;

import edu.kit.tm.pseprak2.alushare.network.coding.CodingHelper;
import edu.kit.tm.pseprak2.alushare.network.protocol.ProtocolConstants;

/**
 * This class decodes the field header and makes the state machine switching to the right sate.
 *
 * @author Albrecht Weiche
 */
public class FieldHeaderState extends DecoderState {
    private byte[] sizeBytes;
    private byte deliminator;
    private int sizeBytesCount;

    /**
     * Creates a new Field header state
     * @param decodeStateMachine the state machine where the state should be changed
     */
    public FieldHeaderState(DecodeStateMachine decodeStateMachine) {
        super(decodeStateMachine, ProtocolConstants.FIELD_HEADER_SIZE);
    }

    @Override
    public void decode(byte[] buffer, int offset, int length) {
        if (sizeBytes == null) {
            deliminator = buffer[offset];
            sizeBytes = new byte[4];
            offset += 1;
            length -= 1;
        }

        int copyCount = ((3 - sizeBytesCount) >= length) ? length : (4 - sizeBytesCount);
        System.arraycopy(buffer, offset, sizeBytes, sizeBytesCount, copyCount);
        sizeBytesCount += copyCount;

        if (sizeBytesCount == 4) {
            int size = CodingHelper.intFromBuffer(sizeBytes);
            switch (deliminator) {
                case ProtocolConstants.FILE:
                    decodeStateMachine.fileState(size);
                    break;
                case ProtocolConstants.CHAT_ID:
                    decodeStateMachine.chatIDState(size);
                    break;
                case ProtocolConstants.FILENAME:
                    decodeStateMachine.fileNameState(size);
                    break;
                case ProtocolConstants.RECEIVER:
                    decodeStateMachine.receiverState(size);
                    break;
                case ProtocolConstants.TEXT:
                    decodeStateMachine.textState(size);
                    break;
                default:
                    decodeStateMachine.errorState();
                    break;
            }
        }
    }

    @Override
    public void onLeave() {
    }

    @Override
    public int write(byte[] buffer, int offset, int length) {
        int readCount = length;
        if (readCount > expectedByteCount()) {
            readCount = expectedByteCount();
        }
        this.decode(buffer, offset, readCount);
        countBytes(readCount);
        return readCount;
    }
}
