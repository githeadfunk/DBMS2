// whether have "sigma", "cross" must exist, if pi's direct children is "sigma", then ...
        //System.out.println(exptree.getChildren().get(0).getSymbol());

        // ------if no where condition-----
        if (exptree.getChildren().get(0).getSymbol() == "cross"){

            // more than 1 table, first need to carry out union
            if (exptree.getChildren().get(0).getChildren().size()>1){
                union();
            }
            // only 1 table, pass the relation and attributes
            else{
                // get relation
                String relationname = exptree.getChildren().get(0).getChildren().get(0).getSymbol();
                if (s.relationExists(relationname)==true){
                    Relation r = s.getRelation(relationname);

                    // get attributes
                    for (int i = 0; i< exptree.getAttribute().size();i++){
                        // check whether the attribute exist in the relation
                        //if ()
                    }

                }
                else{
                    System.err.println("relatoin not exists");
                }

                //System.out.print(r + "\n" + "\n");
            }
        }
        //---- if has where condition------
        else{

        }

        // start from cross
        //int tablenum = 0;
        //tablenum =