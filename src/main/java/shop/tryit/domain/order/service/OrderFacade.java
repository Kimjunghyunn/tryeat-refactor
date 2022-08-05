package shop.tryit.domain.order.service;

import static java.util.stream.Collectors.toList;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import shop.tryit.domain.cart.entity.Cart;
import shop.tryit.domain.cart.service.CartItemService;
import shop.tryit.domain.cart.service.CartService;
import shop.tryit.domain.item.entity.Item;
import shop.tryit.domain.item.service.ItemService;
import shop.tryit.domain.member.entity.Member;
import shop.tryit.domain.member.service.MemberService;
import shop.tryit.domain.order.dto.OrderDetailDto;
import shop.tryit.domain.order.dto.OrderFindDto;
import shop.tryit.domain.order.dto.OrderFindDto.OrderDetailFindDto;
import shop.tryit.domain.order.dto.OrderSearchDto;
import shop.tryit.domain.order.entity.Order;
import shop.tryit.domain.order.entity.OrderDetail;
import shop.tryit.domain.order.entity.OrderStatus;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderFacade {

    private final OrderService orderService;
    private final OrderDetailService orderDetailService;
    private final MemberService memberService;
    private final ItemService itemService;
    private final CartItemService cartItemService;
    private final CartService cartService;

    public Page<OrderSearchDto> searchOrders(int page, User user) {
        Member member = memberService.findMember(user.getUsername());
        return orderService.searchOrders(page, member);
    }

    @Transactional
    public Long register(User user, List<OrderDetailDto> orderDetailDtos) {
        String email = user.getUsername();
        Member member = memberService.findMember(email);

        Long savedOrderId = orderService.register(Order.of(member, OrderStatus.ORDER));
        Order findOrder = orderService.findOne(savedOrderId);

        registerOrderDetail(orderDetailDtos, findOrder);
        deleteCartItems(orderDetailDtos, member);

        return findOrder.getId();
    }

    private void registerOrderDetail(List<OrderDetailDto> orderDetailDtos, Order findOrder) {
        orderDetailDtos.stream()
                .map(orderDetailDto -> toEntity(orderDetailDto, findOrder))
                .forEach(orderDetailService::save);
    }

    private void deleteCartItems(List<OrderDetailDto> orderDetailDtos, Member member) {
        Cart cart = cartService.findCart(member.getEmail());
        orderDetailDtos.forEach(orderDetailDto -> cartItemService.deleteAfterOrder(cart, orderDetailDto.getItemId()));
    }

    public OrderFindDto findOne(Long orderId) {
        Order order = orderService.findOne(orderId);
        return toDto(order);
    }

    private OrderDetail toEntity(OrderDetailDto orderDetailDto, Order order) {
        Long itemId = orderDetailDto.getItemId();
        Item item = itemService.findItem(itemId);

        return OrderDetail.of(item, order, orderDetailDto.getOrderQuantity());
    }

    private OrderFindDto toDto(Order order) {
        List<OrderDetail> orderDetails = orderDetailService.findOrderDetailsByOrder(order);
        List<OrderDetailFindDto> orderDetailFindDtos = orderDetails.stream()
                .map(this::toDto)
                .collect(toList());

        return OrderFindDto.builder()
                .orderId(order.getId())
                .orderNumber(order.getNumber())
                .orderDateTime(order.getCreateDate())
                .orderStatus(order.getStatus())
                .zipcode(order.zipCode())
                .streetAddress(order.streetAddress())
                .detailAddress(order.detailAddress())
                .memberName(order.memberName())
                .memberPhoneNumber(order.memberPhoneNumber())
                .orderDetailFindDtos(orderDetailFindDtos)
                .build();
    }

    private OrderDetailFindDto toDto(OrderDetail orderDetail) {
        return OrderDetailFindDto.builder()
                .itemId(orderDetail.getId())
                .itemName(orderDetail.itemName())
                .orderQuantity(orderDetail.getQuantity())
                .itemMainImage(orderDetail.mainImage())
                .price(orderDetail.itemPrice())
                .build();
    }

}
