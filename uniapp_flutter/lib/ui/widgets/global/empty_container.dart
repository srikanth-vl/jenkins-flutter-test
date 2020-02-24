import 'package:flutter/material.dart';

class EmptyContainer extends StatelessWidget {
  const EmptyContainer();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 0.0,
      height: 0.0,
    );
  }
}