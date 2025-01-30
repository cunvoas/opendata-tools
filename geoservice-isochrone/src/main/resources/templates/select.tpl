        <select class="form-select" required="required"  id="type" name="type" th:field="*{type.id}">
            <option value="" selected disabled th:text="#{contributor.asso.placeholder}">Choose here</option>
            <option th:each="parkType : ${parkTypes}" 
                th:value="${parkType.id}" th:text="${parkType.i18n}" 
                th:selected="${parkType.id==type.id}"></option>
        </select>