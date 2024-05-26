package com.thiru.investment_tracker.operation;

//public class SpecificationBuilder {
//
//    private final List<SearchCriteria> params;
//
//    public SpecificationBuilder(){
//        this.params = new ArrayList<>();
//    }
//
//    public final SpecificationBuilder with(String key,
//                                           String operation, Object value){
//        params.add(new SearchCriteria(key, operation, value));
//        return this;
//    }
//
//    public final SpecificationBuilder with(SearchCriteria
//                                                      searchCriteria){
//        params.add(searchCriteria);
//        return this;
//    }
//
//    public Specification<Employee> build(){
//        if(params.size() == 0){
//            return null;
//        }
//
//        Specification<Asset> result =
//                new EmployeeSpecification(params.get(0));
//        for (int idx = 1; idx < params.size(); idx++){
//            SearchCriteria criteria = params.get(idx);
//            result =  FilterOperation.getDataOption(criteria
//                    .getDataOption()) == FilterOperation.ALL
//                    ? Specification.where(result).and(new
//                    EmployeeSpecification(criteria))
//                    : Specification.where(result).or(
//                    new EmployeeSpecification(criteria));
//        }
//        return result;
//    }
//}