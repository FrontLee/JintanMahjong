package com.vinson.mahjong.base;

import java.util.ArrayList;
import java.util.List;

public class AvailableOperations {
    public int side;
    public List<Operation> operations = new ArrayList<Operation>();
    public List<KongOperation> kongOperations;
    public List<ChowType> chowTypes;
}
